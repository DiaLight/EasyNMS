package dialight.easynms.remap;

import org.objectweb.asm.*;

public class RemapFieldVisitor extends FieldVisitor {

    private final RemapVisitor remap;

    public RemapFieldVisitor(RemapVisitor remap, FieldVisitor fieldVisitor) {
        super(Opcodes.ASM9, fieldVisitor);
        this.remap = remap;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        descriptor = remap.visitDescriptor(descriptor);
        return super.visitAnnotation(descriptor, visible);
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        descriptor = remap.visitDescriptor(descriptor);
        return super.visitTypeAnnotation(typeRef, typePath, descriptor, visible);
    }

    @Override
    public void visitAttribute(Attribute attribute) {
        super.visitAttribute(attribute);
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
    }
}
