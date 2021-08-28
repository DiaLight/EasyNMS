package dialight.easynms.mapping;

import dialight.easynms.mapping.ClassVisitor;

public interface MappingVisitor {

    default void visitStart() {}

    ClassVisitor visitClass(String src, String dst);

    default void visitEnd() {}

}
