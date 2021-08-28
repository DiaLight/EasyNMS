package dialight.easynms.remap;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Opcodes;

public class RemapAnnotationVisitor extends AnnotationVisitor {

    private final RemapVisitor remap;

    public RemapAnnotationVisitor(RemapVisitor remap, AnnotationVisitor annotationVisitor) {
        super(Opcodes.ASM9, annotationVisitor);
        this.remap = remap;
    }

}
