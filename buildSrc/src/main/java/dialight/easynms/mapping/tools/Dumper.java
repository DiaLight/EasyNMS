package dialight.easynms.mapping.tools;

import dialight.easynms.mapping.ClassVisitor;
import dialight.easynms.mapping.MappingVisitor;

import java.util.function.Consumer;

public class Dumper implements MappingVisitor {

    private final Consumer<String> consumer;

    public Dumper(Consumer<String> consumer) {
        this.consumer = consumer;
    }

    @Override
    public ClassVisitor visitClass(String src, String dst) {
        consumer.accept(" %s -> %s".formatted(src, dst));
        return new AnotherMappingFormatWriterClass();
    }

    private class AnotherMappingFormatWriterClass implements ClassVisitor {

        @Override
        public void visitField(String src, String dst, String srcDesc, String dstDesc) {
            String prefix = srcDesc == null ? "" : srcDesc + " ";
            String suffix = dstDesc == null ? "" : " " + dstDesc;
            consumer.accept("  F: %s%s -> %s%s".formatted(prefix, src, dst, suffix));
        }

        @Override
        public void visitMethod(String src, String dst, String srcDesc, String dstDesc) {
            String suffix = dstDesc == null ? "" : " " + dstDesc;
            consumer.accept("  M: %s %s -> %s%s".formatted(srcDesc, src, dst, suffix));
        }

    }

}
