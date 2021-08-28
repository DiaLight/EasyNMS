package dialight.easynms;

import dialight.easynms.mapping.*;
import dialight.easynms.mapping.io.AnotherMappingFormatWriter;
import dialight.easynms.mapping.utils.Mappings;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.signature.SignatureVisitor;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;
import dialight.easynms.tasks.remapjar.InheritGraph;

/**
 * {@link org.objectweb.asm.commons.Remapper} gives incorrect descriptor for overriden method with generic return value
 *
 * example: public class LightningStrikeTrigger extends CriterionTriggerAbstract<LightningStrikeTrigger.a>
 * == class overriden method mapping
 * owner: net/minecraft/advancements/critereon/LightningStrikeTrigger -> net/minecraft/advancements/critereon/LightningStrikeTrigger
 * name: b -> createInstance
 * desc: (Lcom/google/gson/JsonObject;Lnet/minecraft/advancements/critereon/CriterionConditionEntity$b;Lnet/minecraft/advancements/critereon/LootDeserializationContext;)Lnet/minecraft/advancements/critereon/CriterionInstanceAbstract;
 *    -> (Lcom/google/gson/JsonObject;Lnet/minecraft/advancements/critereon/EntityPredicate$Composite;Lnet/minecraft/advancements/critereon/DeserializationContext;)Lnet/minecraft/advancements/critereon/AbstractCriterionTriggerInstance;
 *
 * == super class method mapping
 * owner: net/minecraft/advancements/critereon/SimpleCriterionTrigger -> net/minecraft/advancements/critereon/CriterionTriggerAbstract
 * name: b -> createInstance
 * desc: (Lcom/google/gson/JsonObject;Lnet/minecraft/advancements/critereon/CriterionConditionEntity$b;Lnet/minecraft/advancements/critereon/LootDeserializationContext;)Lnet/minecraft/advancements/critereon/CriterionInstanceAbstract;
 *    -> (Lcom/google/gson/JsonObject;Lnet/minecraft/advancements/critereon/EntityPredicate$Composite;Lnet/minecraft/advancements/critereon/DeserializationContext;)Lnet/minecraft/advancements/critereon/AbstractCriterionTriggerInstance;
 *
 * == incorrect mapMethodName arguments
 * owner: net/minecraft/advancements/critereon/LightningStrikeTrigger
 * name: b
 * descriptor: (Lcom/google/gson/JsonObject;Lnet/minecraft/advancements/critereon/CriterionConditionEntity$b;Lnet/minecraft/advancements/critereon/LootDeserializationContext;)Lnet/minecraft/advancements/critereon/LightningStrikeTrigger$a;
 *
 * Use {@link EasyRemapper} instead
 * */

@Deprecated
public class EasyAsmRemapper extends Remapper {

    private final Mapping mapping;
    private final InheritGraph inheritGraph;
    private final Mapping adaptedMapping;

    public EasyAsmRemapper(Mapping mapping, InheritGraph inheritGraph) {
        this.mapping = mapping;
        this.inheritGraph = inheritGraph;
        this.adaptedMapping = new Mapping();
        // invert class name and ignore members descriptors
        final AbstractMappingVisitor visitor = new AbstractMappingVisitor(this.adaptedMapping) {
            @Override
            public ClassVisitor visitClass(String src, String dst) {
                final var classVisitor = super.visitClass(dst, src);
                return new AbstractClassVisitor(classVisitor) {
                    @Override
                    public void visitField(String src, String dst, String srcDesc, String dstDesc) {
                        super.visitField(src, dst, srcDesc, dstDesc);
                    }

                    @Override
                    public void visitMethod(String src, String dst, String srcDesc, String dstDesc) {
                        super.visitMethod(src, dst, srcDesc, dstDesc);
                    }
                };
            }
        };
        visitor.visitStart();
        mapping.accept(visitor);
        visitor.visitEnd();
        try {
            AnotherMappingFormatWriter.write(Paths.get("F:\\workspace\\EasyNMS\\build\\easynms\\mappings\\1.17.1-adapted.amf"), this.adaptedMapping);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String mapDesc(String descriptor) {
        return super.mapDesc(descriptor);
//        return Mappings.remapDesc(mapping, descriptor);
    }

    @Override
    public String mapType(String internalName) {
        return super.mapType(internalName);
//        final var clazz = this.mapping.getClass(internalName);
//        if(clazz != null) internalName = clazz.dst;
//        return internalName;
    }

    @Override
    public String[] mapTypes(String[] internalNames) {
        return super.mapTypes(internalNames);
    }

    @Override
    public String mapMethodDesc(String methodDescriptor) {
//        return Mappings.remapDesc(mapping, methodDescriptor);
        return super.mapMethodDesc(methodDescriptor);
    }

    @Override
    public Object mapValue(Object value) {
        return super.mapValue(value);
    }

    @Override
    public String mapSignature(String signature, boolean typeSignature) {
//        if (signature == null) return null;
//        return Mappings.remapSignature(mapping, signature);
        return super.mapSignature(signature, typeSignature);
    }

    @Override
    protected SignatureVisitor createRemappingSignatureAdapter(SignatureVisitor signatureVisitor) {
        return super.createRemappingSignatureAdapter(signatureVisitor);
    }

    @Override
    protected SignatureVisitor createSignatureRemapper(SignatureVisitor signatureVisitor) {
        return super.createSignatureRemapper(signatureVisitor);
    }

    @Override
    public String mapAnnotationAttributeName(String descriptor, String name) {
//        System.out.println("unmapped AnnotationAttributeName " + descriptor + " " + name);
        return name;
//        return super.mapAnnotationAttributeName(descriptor, name);
    }

    @Override
    public String mapInnerClassName(String name, String ownerName, String innerName) {
        final var $Idx = name.indexOf('$');
        if($Idx == -1) throw new RuntimeException(name + " ??" + "$" + innerName + " $ not found");
        if(ownerName == null) {
            ownerName = name.substring(0, $Idx);
        }// 278
        final var remapped = super.mapInnerClassName(name, ownerName, innerName);
        if (Objects.equals(ownerName, "net/minecraft/advancements/CriterionTrigger")) {
            System.out.println(name + " " + ownerName + " " + innerName + " -> " + remapped);
            // remap owner to spigot
            final var owner = this.adaptedMapping.getClass(ownerName);
            if(owner != null) {
                ownerName = owner.dst;
                System.out.println(owner.dst + name.substring($Idx));
            }
//            final var clazz = this.mapping.getClass(name);
//            if(clazz != null) {
//                final var $Idx2 = clazz.dst.indexOf('$');
//                if($Idx2 == -1) throw new RuntimeException(clazz.src + " -> " + clazz.dst + " $ not found");
//                return clazz.dst.substring($Idx2 + 1);
//            }
        }

//        if(!Objects.equals(name, ownerName + "$" + innerName)) {
//            innerClasses.add("err: " + name + " != " + ownerName + "$" + innerName + " -> " + remapped);
////            throw new RuntimeException(name + " != " + ownerName + "$" + innerName);
//        } else {
//            innerClasses.add(name + " -> " + remapped);
//        }
        return remapped;
    }

    public final ConcurrentLinkedQueue<String> unmappedMethods = new ConcurrentLinkedQueue<>();

    @Override
    public String mapMethodName(String owner, String name, String descriptor) {
        List<String> owners = inheritGraph.collectSuperClasses(owner, true);
        boolean visited = false;
        for (String candidate : owners) {
            final var clazz = this.adaptedMapping.getClass(candidate);
            if(clazz != null) {
                visited = true;
                final var method = clazz.getMethod(name, descriptor);
                if(method != null) return method.dst;
            }
        }
        if(visited && isObfuscated(name)) {
            unmappedMethods.add(owner + " " + name + " " + descriptor);
        }
        return name;
//        return super.mapMethodName(owner, name, descriptor);
    }

    private boolean isObfuscated(String name) {
        if(name.length() != 1) return false;
        final var c = name.charAt(0);
        return c == 'a' || c == 'b' || c == 'c' || c == 'A';
    }

    @Override
    public String mapInvokeDynamicMethodName(String name, String descriptor) {
//        System.out.println("unmapped InvokeDynamicMethodName " + name + " " + descriptor);
        return name;
//        return super.mapInvokeDynamicMethodName(name, descriptor);
    }

    @Override
    public String mapRecordComponentName(String owner, String name, String descriptor) {
//        System.out.println("unmapped RecordComponentName " + owner + " " + name + " " + descriptor);
        return name;
//        return super.mapRecordComponentName(owner, name, descriptor);
    }

    @Override
    public String mapFieldName(String owner, String name, String descriptor) {
//        if(owner.startsWith("net")) System.out.println("!! " + owner + " " + name + " " + descriptor);
        final var clazz = this.adaptedMapping.getClass(owner);
        if(clazz != null) {
            final var field = clazz.getField(name);
            if(field != null) return field.dst;
//            if(name.equals("a")) {
//                System.out.println("unmapped field " + owner + " " + name + " " + descriptor);
//            }
        }
        return name;
//        return super.mapFieldName(owner, name, descriptor);
    }

    @Override
    public String mapPackageName(String name) {
//        System.out.println("unmapped PackageName " + name);
        return name;
//        return super.mapPackageName(name);
    }

    @Override
    public String mapModuleName(String name) {
//        System.out.println("unmapped ModuleName " + name);
        return name;
//        return super.mapModuleName(name);
    }

    @Override
    public String map(String internalName) {
        final var clazz = this.mapping.getClass(internalName);
        if(clazz != null) internalName = clazz.dst;
        return internalName;
//        return super.map(internalName);
    }
}
