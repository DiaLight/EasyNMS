package dialight.easynms.remap;

import org.jetbrains.annotations.Nullable;

public interface RemapVisitor {

    interface ClassRemapVisitor {

        String getDst();
        String visitMethod(String name, String descriptor);
        String visitField(String name, String descriptor);

    }

    @Nullable
    ClassRemapVisitor getClassVisitor(String name);

    String visitClass(String name);
    String visitSignature(String signature);
    String visitDescriptor(String descriptor);

}
