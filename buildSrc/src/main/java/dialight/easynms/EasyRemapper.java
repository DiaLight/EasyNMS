package dialight.easynms;

import dialight.easynms.mapping.*;
import dialight.easynms.mapping.utils.Mappings;
import dialight.easynms.remap.RemapSignatureVisitor;
import dialight.easynms.remap.RemapVisitor;
import dialight.easynms.tasks.remapjar.InheritGraph;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;

public class EasyRemapper implements RemapVisitor {

    private final Mapping mapping;
    private final InheritGraph inheritGraph;

    public final ConcurrentLinkedQueue<String> unmappedMethods = new ConcurrentLinkedQueue<>();

    public EasyRemapper(Mapping mapping, InheritGraph inheritGraph) {
        this.inheritGraph = inheritGraph;
        this.mapping = new Mapping();
        this.mapping.visitStart();
        mapping.accept(new AbstractMappingVisitor(this.mapping) {
            @Override
            public ClassVisitor visitClass(String src, String dst) {
                return new AbstractClassVisitor(super.visitClass(src, dst)) {

                    private String remapRef(String className) {
                        int last$Idx = className.lastIndexOf('$');
                        while(last$Idx != -1) {
                            String outerName = className.substring(0, last$Idx);
                            final var clazz = mapping.getClass(outerName);
                            if(clazz != null) {
                                return clazz.dst + '$' + className.substring(last$Idx + 1);
                            }
                            last$Idx = className.lastIndexOf('$', last$Idx - 1);
                        }
                        final var clazz = mapping.getClass(className);
                        if(clazz != null) return clazz.dst;
                        return className;
                    }

                    @Override
                    public void visitField(String src, String dst, String srcDesc, String dstDesc) {
                        super.visitField(src, dst, srcDesc, dstDesc);
                    }

                    @Override
                    public void visitMethod(String src, String dst, String srcDesc, String dstDesc) {
//                        if(dstDesc.equals("(I)Lnet/minecraft/advancements/AdvancementRewards$Builder;")) {
//                            System.out.println();
//                        }
                        super.visitMethod(src, dst, srcDesc, dstDesc);
//                        final var remappedDesc = Mappings.remapDesc(srcDesc, this::remapRef);
//                        super.visitMethod(src, dst, remappedDesc, dstDesc);
                    }
                };
            }
        });
        this.mapping.visitEnd();
    }

    @Override
    public ClassRemapVisitor getClassVisitor(String name) {
        List<Clazz> supers = inheritGraph.collectSuperClasses(name, false)
                .stream().map(this.mapping::getClass).filter(Objects::nonNull).toList();
        return new Class(name, this.mapping.getClass(name), supers);
    }

    @Override
    public String visitClass(String name) {
        final var clazz = this.mapping.getClass(name);
        if(clazz != null) name = clazz.dst;
        return name;
    }

    @Override
    public String visitSignature(String signature) {
        return RemapSignatureVisitor.remap(this, signature);
//        return Mappings.remapSignature(mapping, signature);
    }

    @Override
    public String visitDescriptor(String descriptor) {
        return Mappings.remapDesc(mapping, descriptor);
    }

    private class Class implements ClassRemapVisitor {

        private final String name;
        @Nullable
        private final Clazz clazz;
        private final List<Clazz> supers;

        public Class(String name, Clazz clazz, List<Clazz> supers) {
            this.name = name;
            this.clazz = clazz;
            this.supers = supers;

        }

        @Override
        public String getDst() {
            if(clazz != null) return clazz.dst;
            return name;
        }

        public String resolveMethod(String name, String descriptor) {
            if(clazz != null) {
                final var method = clazz.getMethod(name, descriptor);
                if(method != null) return method.dst;
            }
            for (var clazz : supers) {
                final var method = clazz.getMethod(name, descriptor);
                if(method != null) return method.dst;
            }
            return null;
        }
        @Override
        public String visitMethod(String name, String descriptor) {
            var newName = resolveMethod(name, descriptor);
            if(newName != null) return newName;
            if(name.length() == 1) {
                // proguard mapping bug
                var fixedName = "" + Character.toString(name.charAt(0) + 1);
                newName = resolveMethod(fixedName, descriptor);
                if(newName != null) return newName;
                fixedName = "" + Character.toString(name.charAt(0) - 1);
                newName = resolveMethod(fixedName, descriptor);
                if(newName != null) return newName;
            }
            if("net/minecraft/core/BlockPosition b (DDD)Lnet/minecraft/core/BlockPosition;".equals(this.name + " " + name + " " + descriptor)) {
                System.out.println();


                if(clazz != null) {
                    final var method = clazz.getMethod(name, descriptor);
                    if(method != null) return method.dst;
                }
            }
            if(isObfuscated(name)) {
                unmappedMethods.add(this.name + " " + name + " " + descriptor);
            }
            return name;
        }

        @Override
        public String visitField(String name, String descriptor) {
            if (clazz != null) {
                final var field = clazz.getField(name);
                if (field != null) return field.dst;
            }
            return name;
        }

    }

    private static boolean isObfuscated(String name) {
        if(name.length() != 1) return false;
        final var c = name.charAt(0);
        return c == 'a' || c == 'b' || c == 'c' || c == 'A';
    }

}
