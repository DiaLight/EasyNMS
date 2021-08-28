package dialight.easynms.remap;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;
import org.objectweb.asm.signature.SignatureWriter;

public class RemapSignatureVisitor extends SignatureVisitor {

    private final RemapVisitor remap;
    private final SignatureVisitor visitor;

    public RemapSignatureVisitor(RemapVisitor remap, SignatureVisitor visitor) {
        super(Opcodes.ASM9);
        this.remap = remap;
        this.visitor = visitor;
    }

    @Override
    public void visitFormalTypeParameter(String name) {
        visitor.visitFormalTypeParameter(name);
    }

    @Override
    public SignatureVisitor visitClassBound() {
        return visitor.visitClassBound();
    }

    @Override
    public SignatureVisitor visitInterfaceBound() {
        return visitor.visitInterfaceBound();
    }

    @Override
    public SignatureVisitor visitSuperclass() {
        return visitor.visitSuperclass();
    }

    @Override
    public SignatureVisitor visitInterface() {
        return visitor.visitInterface();
    }

    @Override
    public SignatureVisitor visitParameterType() {
        return visitor.visitParameterType();
    }

    @Override
    public SignatureVisitor visitReturnType() {
        return visitor.visitReturnType();
    }

    @Override
    public SignatureVisitor visitExceptionType() {
        return visitor.visitExceptionType();
    }

    @Override
    public void visitBaseType(char descriptor) {
        visitor.visitBaseType(descriptor);
    }

    @Override
    public void visitTypeVariable(String name) {
        visitor.visitTypeVariable(remap.visitClass(name));
    }

    @Override
    public SignatureVisitor visitArrayType() {
        return visitor.visitArrayType();
    }

    @Override
    public void visitClassType(String name) {
        visitor.visitClassType(remap.visitClass(name));
    }

    @Override
    public void visitInnerClassType(String name) {
        visitor.visitClassType(remap.visitClass(name));
    }

    @Override
    public void visitTypeArgument() {
        visitor.visitTypeArgument();
    }

    @Override
    public SignatureVisitor visitTypeArgument(char wildcard) {
        return visitor.visitTypeArgument(wildcard);
    }

    @Override
    public void visitEnd() {
        visitor.visitEnd();
    }

    public static String remapType(RemapVisitor remap, String signature) {
        SignatureReader signatureReader = new SignatureReader(signature);
        SignatureWriter signatureWriter = new SignatureWriter();
        final var signatureVisitor = new RemapSignatureVisitor(remap, signatureWriter);
        signatureReader.acceptType(signatureVisitor);
        return signatureWriter.toString();
    }

    public static String remap(RemapVisitor remap, String signature) {
        SignatureReader signatureReader = new SignatureReader(signature);
        SignatureWriter signatureWriter = new SignatureWriter();
        final var signatureVisitor = new RemapSignatureVisitor(remap, signatureWriter);
        signatureReader.accept(signatureVisitor);
        return signatureWriter.toString();
    }

}
