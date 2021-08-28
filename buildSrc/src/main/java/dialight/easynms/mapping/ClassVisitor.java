package dialight.easynms.mapping;

import org.jetbrains.annotations.NotNull;

public interface ClassVisitor {

    default void visitStart() {}

    void visitField(String src, String dst, String srcDesc, String dstDesc);

    void visitMethod(String src, String dst, String srcDesc, String dstDesc);

    default void visitEnd() {}

}
