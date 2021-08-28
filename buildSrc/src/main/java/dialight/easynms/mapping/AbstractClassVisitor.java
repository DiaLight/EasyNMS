package dialight.easynms.mapping;

public abstract class AbstractClassVisitor implements ClassVisitor {

    private final ClassVisitor visitor;

    public AbstractClassVisitor(ClassVisitor visitor) {
        this.visitor = visitor;
    }

    @Override
    public void visitStart() {
        visitor.visitStart();
    }

    @Override
    public void visitField(String src, String dst, String srcDesc, String dstDesc) {
        visitor.visitField(src, dst, srcDesc, dstDesc);
    }

    @Override
    public void visitMethod(String src, String dst, String srcDesc, String dstDesc) {
        visitor.visitMethod(src, dst, srcDesc, dstDesc);
    }

    @Override
    public void visitEnd() {
        visitor.visitEnd();
    }

}
