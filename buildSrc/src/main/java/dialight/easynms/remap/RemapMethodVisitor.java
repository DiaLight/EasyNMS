package dialight.easynms.remap;

import org.objectweb.asm.*;

public class RemapMethodVisitor extends MethodVisitor {

    private final RemapVisitor remap;

    public RemapMethodVisitor(RemapVisitor remap, MethodVisitor visitor) {
        super(Opcodes.ASM9, visitor);
        this.remap = remap;
    }

    @Override
    public void visitParameter(String name, int access) {
        super.visitParameter(name, access);
    }

    @Override
    public AnnotationVisitor visitAnnotationDefault() {
        return new RemapAnnotationVisitor(remap, super.visitAnnotationDefault());
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        return new RemapAnnotationVisitor(remap, super.visitAnnotation(descriptor, visible));
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        return super.visitTypeAnnotation(typeRef, typePath, descriptor, visible);
    }

    @Override
    public void visitAnnotableParameterCount(int parameterCount, boolean visible) {
        super.visitAnnotableParameterCount(parameterCount, visible);
    }

    @Override
    public AnnotationVisitor visitParameterAnnotation(int parameter, String descriptor, boolean visible) {
        return new RemapAnnotationVisitor(remap, super.visitParameterAnnotation(parameter, descriptor, visible));
    }

    @Override
    public void visitAttribute(Attribute attribute) {
        super.visitAttribute(attribute);
    }

    @Override
    public void visitFrame(int type, int numLocal, Object[] local, int numStack, Object[] stack) {
        super.visitFrame(type, numLocal, local, numStack, stack);
    }

    @Override
    public void visitTypeInsn(int opcode, String type) {
        if(type.startsWith("[") || type.startsWith("L")) {
            type = remap.visitDescriptor(type);
        } else {
            type = remap.visitClass(type);
        }
        super.visitTypeInsn(opcode, type);
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
        final var classVisitor = remap.getClassVisitor(owner);
        if (classVisitor != null) {
            owner = classVisitor.getDst();
            name = classVisitor.visitField(name, descriptor);
        }
        descriptor = remap.visitDescriptor(descriptor);
        super.visitFieldInsn(opcode, owner, name, descriptor);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        final var classVisitor = remap.getClassVisitor(owner);
        if (classVisitor != null) {
            owner = classVisitor.getDst();
            name = classVisitor.visitMethod(name, descriptor);
        }
        descriptor = remap.visitDescriptor(descriptor);
        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
        // TODO: dyn handle
        descriptor = remap.visitDescriptor(descriptor);
        super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
    }

    @Override
    public void visitMultiANewArrayInsn(String descriptor, int numDimensions) {
        descriptor = remap.visitDescriptor(descriptor);
        super.visitMultiANewArrayInsn(descriptor, numDimensions);
    }

    @Override
    public AnnotationVisitor visitInsnAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        return new RemapAnnotationVisitor(remap, super.visitInsnAnnotation(typeRef, typePath, descriptor, visible));
    }

    @Override
    public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
        type = remap.visitClass(type);
        super.visitTryCatchBlock(start, end, handler, type);
    }

    @Override
    public AnnotationVisitor visitTryCatchAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        descriptor = remap.visitDescriptor(descriptor);
        return new RemapAnnotationVisitor(remap, super.visitTryCatchAnnotation(typeRef, typePath, descriptor, visible));
    }

    @Override
    public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {
        descriptor = remap.visitDescriptor(descriptor);
        if(signature != null) signature = remap.visitSignature(signature);
        super.visitLocalVariable(name, descriptor, signature, start, end, index);
    }

    @Override
    public AnnotationVisitor visitLocalVariableAnnotation(int typeRef, TypePath typePath, Label[] start, Label[] end, int[] index, String descriptor, boolean visible) {
        descriptor = remap.visitDescriptor(descriptor);
        return new RemapAnnotationVisitor(remap, super.visitLocalVariableAnnotation(typeRef, typePath, start, end, index, descriptor, visible));
    }

}
