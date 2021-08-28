package dialight.easynms.remap;

import org.objectweb.asm.ModuleVisitor;
import org.objectweb.asm.Opcodes;

public class RemapModuleVisitor extends ModuleVisitor {

    private final RemapVisitor remap;

    public RemapModuleVisitor(RemapVisitor remap, ModuleVisitor visitor) {
        super(Opcodes.ASM9, visitor);
        this.remap = remap;
    }

}
