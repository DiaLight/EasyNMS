package dialight.easynms.mapping;

public abstract class AbstractMappingVisitor implements MappingVisitor {

    private final MappingVisitor visitor;

    public AbstractMappingVisitor(MappingVisitor visitor) {
        this.visitor = visitor;
    }

    @Override
    public void visitStart() {
        this.visitor.visitStart();
    }

    @Override
    public ClassVisitor visitClass(String src, String dst) {
        return this.visitor.visitClass(src, dst);
    }

    @Override
    public void visitEnd() {
        this.visitor.visitEnd();
    }

}
