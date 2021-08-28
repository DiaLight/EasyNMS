package dialight.easynms.mapping.io;

import dialight.easynms.mapping.ClassVisitor;
import dialight.easynms.mapping.Mapping;
import dialight.easynms.mapping.MappingVisitor;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

public class AnotherMappingFormatWriter implements MappingVisitor {

    private final Consumer<String> consumer;

    public AnotherMappingFormatWriter(Consumer<String> consumer) {
        this.consumer = consumer;
    }

    @Override
    public ClassVisitor visitClass(String src, String dst) {
        consumer.accept("CL: " + src + " " + dst);
        return new AnotherMappingFormatWriterClass();
    }

    private class AnotherMappingFormatWriterClass implements ClassVisitor {

        @Override
        public void visitField(String src, String dst, String srcDesc, String dstDesc) {
            consumer.accept("  FD: " + src + " " + dst + " " + srcDesc + " " + dstDesc);
        }

        @Override
        public void visitMethod(String src, String dst, String srcDesc, String dstDesc) {
            consumer.accept("  MD: " + src + " " + dst + " " + srcDesc + " " + dstDesc);
        }

    }

    public static void write(Path file, Mapping mapping) throws IOException {
        try(BufferedWriter writer = Files.newBufferedWriter(file)) {
            final var amfWriter = new AnotherMappingFormatWriter(line -> {
                try {
                    writer.write(line);
                    writer.newLine();
                } catch (IOException e) { throw new RuntimeException(e); }
            });
            amfWriter.visitStart();
            mapping.accept(amfWriter);
            amfWriter.visitEnd();
        }
    }

}
