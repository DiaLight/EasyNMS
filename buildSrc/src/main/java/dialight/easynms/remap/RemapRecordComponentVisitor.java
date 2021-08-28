package dialight.easynms.remap;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.RecordComponentVisitor;

public class RemapRecordComponentVisitor extends RecordComponentVisitor {

    private final RemapVisitor remap;

    public RemapRecordComponentVisitor(RemapVisitor remap, RecordComponentVisitor recordComponentVisitor) {
        super(Opcodes.ASM9, recordComponentVisitor);
        this.remap = remap;
    }

}
