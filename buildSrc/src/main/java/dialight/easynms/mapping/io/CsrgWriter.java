package dialight.easynms.mapping.io;

import dialight.easynms.mapping.ClassVisitor;
import dialight.easynms.mapping.MappingVisitor;

import java.util.function.Consumer;

public class CsrgWriter implements MappingVisitor {

    private final Consumer<String> consumer;
    private final boolean members;

    public CsrgWriter(Consumer<String> consumer, boolean members) {
        this.consumer = consumer;
        this.members = members;
    }

    @Override public ClassVisitor visitClass(String src, String dst) {
        if(!members) consumer.accept(src + " " + dst);
        return new ProGuardWriterClass(src);
    }

    private class ProGuardWriterClass implements ClassVisitor {

        private final String className;

        public ProGuardWriterClass(String className) {
            this.className = className;
        }

        @Override public void visitField(String src, String dst, String srcDesc, String dstDesc) {
            if(!members) return;
            if(srcDesc == null) throw new NullPointerException("srcDesc == null");
            consumer.accept(className + " " + src + " " + srcDesc + " " + dst);
        }

        @Override public void visitMethod(String src, String dst, String srcDesc, String dstDesc) {
            if(!members) return;
            consumer.accept(className + " " + src + " " + srcDesc + " " + dst);
        }

    }


}
