package dialight.easynms.mapping.io;

import dialight.easynms.mapping.ClassVisitor;
import dialight.easynms.mapping.Mapping;
import dialight.easynms.mapping.MappingVisitor;
import org.objectweb.asm.Type;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ProguardWriter implements MappingVisitor {

    private final Consumer<String> consumer;

    public ProguardWriter(Consumer<String> consumer) {
        this.consumer = consumer;
    }

    @Override public ClassVisitor visitClass(String src, String dst) {
        consumer.accept(src.replace('/', '.') + " -> " + dst.replace('/', '.') + ":");
        return new ProGuardWriterClass();
    }

    private class ProGuardWriterClass implements ClassVisitor {

        @Override public void visitField(String src, String dst, String srcDesc, String dstDesc) {
            if(srcDesc == null) throw new NullPointerException("desc == null");
            final var type = Type.getType(srcDesc);
            consumer.accept("    " + type.getClassName() + " " + src + " -> " + dst);
        }

        @Override public void visitMethod(String src, String dst, String srcDesc, String dstDesc) {
            final var type = Type.getMethodType(srcDesc);
            consumer.accept("    " + type.getReturnType().getClassName() + " " + src + "("
                    + Arrays.stream(type.getArgumentTypes()).map(Type::getClassName).collect(Collectors.joining(","))
                    + ") -> " + dst);
        }

    }

    public static void write(Path file, Mapping mapping) throws IOException {
        try(BufferedWriter writer = Files.newBufferedWriter(file)) {
            final var proguardWriter = new ProguardWriter(line -> {
                try {
                    writer.write(line);
                    writer.newLine();
                } catch (IOException e) { throw new RuntimeException(e); }
            });
            proguardWriter.visitStart();
            mapping.accept(proguardWriter);
            proguardWriter.visitEnd();
        }
    }

}
