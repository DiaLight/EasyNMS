package dialight.easynms.mapping;

import dialight.easynms.mapping.Clazz;
import dialight.easynms.mapping.MappingVisitor;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Mapping implements MappingVisitor {

    private final Map<String, Clazz> mapping = new HashMap<>();

    public Clazz visitClass(String src, String dst) {
        Clazz clazz = new Clazz(src, dst);
        mapping.put(src, clazz);
        return clazz;
    }

    public Collection<Clazz> getClasses() {
        return mapping.values();
    }

    @Nullable
    public Clazz getClass(String src) {
        return mapping.get(src);
    }

    @NotNull
    public Clazz requireClass(String src) {
        Clazz clazz = mapping.get(src);
        if(clazz == null) throw new NullPointerException(src);
        return clazz;
    }

    public void accept(MappingVisitor visitor) {
        for (Clazz clazz : mapping.values()) {
            final var classVisitor = visitor.visitClass(clazz.src, clazz.dst);
            classVisitor.visitStart();
            clazz.accept(classVisitor);
            classVisitor.visitEnd();
        }
    }

}
