package dialight.easynms.mapping.tools;

import dialight.easynms.mapping.ClassVisitor;
import dialight.easynms.mapping.MappingVisitor;

public class InvertProxy implements MappingVisitor {

    private final MappingVisitor visitor;

    public InvertProxy(MappingVisitor visitor) {
        this.visitor = visitor;
    }

    @Override
    public void visitStart() {
        this.visitor.visitStart();
    }

    @Override
    public ClassVisitor visitClass(String src, String dst) {
        return new InvertClassProxy(this.visitor.visitClass(dst, src));
    }

    @Override
    public void visitEnd() {
        this.visitor.visitEnd();
    }

    private static class InvertClassProxy implements ClassVisitor {

        private final ClassVisitor visitor;

        public InvertClassProxy(ClassVisitor visitor) {
            this.visitor = visitor;
        }

        @Override
        public void visitStart() {
            this.visitor.visitStart();
        }

        @Override
        public void visitField(String src, String dst, String srcDesc, String dstDesc) {
            this.visitor.visitField(dst, src, dstDesc, srcDesc);
        }

        @Override
        public void visitMethod(String src, String dst, String srcDesc, String dstDesc) {
            this.visitor.visitMethod(dst, src, dstDesc, srcDesc);
        }

        @Override
        public void visitEnd() {
            this.visitor.visitEnd();
        }

    }

}
