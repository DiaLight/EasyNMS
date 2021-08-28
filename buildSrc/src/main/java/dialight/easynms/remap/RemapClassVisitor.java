package dialight.easynms.remap;

import dialight.easynms.mapping.Clazz;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.*;

import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;

public class RemapClassVisitor extends ClassVisitor {

    private final RemapVisitor remap;
    @Nullable
    private RemapVisitor.ClassRemapVisitor currentClass;
    ConcurrentLinkedQueue<String> unvisited;

    public RemapClassVisitor(RemapVisitor remap, ClassVisitor visitor, ConcurrentLinkedQueue<String> unvisited) {
        super(Opcodes.ASM9, visitor);
        this.remap = remap;
        this.unvisited = unvisited;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.currentClass = remap.getClassVisitor(name);
        if(this.currentClass != null) name = this.currentClass.getDst();

        if(superName != null) superName = remap.visitClass(superName);
        if(interfaces != null) {
            for (int i = 0; i < interfaces.length; i++) {
                interfaces[i] = remap.visitClass(interfaces[i]);
            }
        }
        if(signature != null) signature = remap.visitSignature(signature);
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public void visitSource(String source, String debug) {
        unvisited.add("visitSource " + source + " " + debug);
        super.visitSource(source, debug);
    }

    @Override
    public ModuleVisitor visitModule(String name, int access, String version) {
//        unvisited.add("visitModule " + name + " " + version);
//        return new RemapModuleVisitor(remap, super.visitModule(name, access, version));
        throw new IllegalStateException("unimplemented");
    }

    @Override
    public void visitNestHost(String nestHost) {
        nestHost = remap.visitClass(nestHost);
        super.visitNestHost(nestHost);
    }

    @Override  // visit method in outer class from inner class
    public void visitOuterClass(String owner, String name, String descriptor) {
        var oldName = name;
        if(name != null) {
            final var classVisitor = this.remap.getClassVisitor(owner);
            if(classVisitor != null) name = classVisitor.visitMethod(name, descriptor);
        }
        if(owner != null) owner = remap.visitClass(owner);
        if(descriptor != null) descriptor = remap.visitDescriptor(descriptor);
        super.visitOuterClass(owner, name, descriptor);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        return new RemapAnnotationVisitor(remap, super.visitAnnotation(descriptor, visible));
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        return new RemapAnnotationVisitor(remap, super.visitTypeAnnotation(typeRef, typePath, descriptor, visible));
    }

    @Override
    public void visitAttribute(Attribute attribute) {
        unvisited.add("visitAttribute " + attribute);
        super.visitAttribute(attribute);
    }

    @Override
    public void visitNestMember(String nestMember) {
        String oldNestMember = nestMember;
        nestMember = remap.visitClass(nestMember);
        if(Objects.equals(nestMember, oldNestMember)) {
            int last$Idx = nestMember.length();
            while(true) {
                last$Idx = nestMember.lastIndexOf('$', last$Idx - 1);
                if(last$Idx == -1) break;
                String outerName = nestMember.substring(0, last$Idx);
                String oldOuterName = outerName;
                outerName = remap.visitClass(outerName);
                if(!Objects.equals(outerName, oldOuterName)) {
                    nestMember = outerName + '$' + nestMember.substring(last$Idx + 1);
                    break;
                }
            }
        }
        super.visitNestMember(nestMember);
    }

    @Override
    public void visitPermittedSubclass(String permittedSubclass) {
        unvisited.add("visitPermittedSubclass " + permittedSubclass);
        super.visitPermittedSubclass(permittedSubclass);
    }

    @Override
    public void visitInnerClass(String name, String outerName, String innerName, int access) {
        final var oldName = name;
        name = remap.visitClass(name);
        if(!Objects.equals(name, oldName)) {
            // take everything from remapped name
            final var $Idx = name.indexOf('$');
            if($Idx == -1) throw new RuntimeException(name + " ??" + "$" + innerName + " $ not found");
            final var last$Idx = name.lastIndexOf('$');
            outerName = name.substring(0, last$Idx);
            int index = last$Idx + 1;
            while (index < name.length()) {
                if(!Character.isDigit(name.charAt(index))) {
                    innerName = name.substring(index);
                    break;
                }
                index++;
            }
//            unvisited.add("visitInnerClass " + oldName + " -d> " + name + " " + outerName + " " + innerName);
        } else {
            // remap parts and build name
            final var last$Idx = name.lastIndexOf('$');
            if(last$Idx == -1) throw new RuntimeException(name + " ??" + "$" + innerName + " $ not found");
            // ensure outer and inner != null
            if(outerName == null) outerName = name.substring(0, last$Idx);
            final var digits = new StringBuilder();
            int index = last$Idx + 1;
            while (index < name.length()) {
                final var ch = name.charAt(index);
                if(!Character.isDigit(ch)) {
                    if(innerName == null) innerName = name.substring(index);
                    break;
                }
                digits.append(ch);
                index++;
            }
            outerName = remap.visitClass(outerName);
            name = outerName + '$' + digits + innerName;

            if(Objects.equals(name, oldName)) {
//                unvisited.add("visitInnerClass " + name + " " + outerName + " " + innerName);
            } else {
//                unvisited.add("visitInnerClass " + oldName + " -> " + name + " " + outerName + " " + innerName);
            }
        }

        super.visitInnerClass(name, outerName, innerName, access);
    }

    @Override
    public RecordComponentVisitor visitRecordComponent(String name, String descriptor, String signature) {
        unvisited.add("visitRecordComponent " + name + " " + descriptor + " " + signature);
        return new RemapRecordComponentVisitor(remap, super.visitRecordComponent(name, descriptor, signature));
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        if(currentClass != null) {
            name = currentClass.visitField(name, descriptor);
            descriptor = remap.visitDescriptor(descriptor);
            if(signature != null) signature = remap.visitSignature(signature);
        }
        return new RemapFieldVisitor(remap, super.visitField(access, name, descriptor, signature, value));
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        if(currentClass != null) {
            name = currentClass.visitMethod(name, descriptor);
            descriptor = remap.visitDescriptor(descriptor);
            if(signature != null) signature = remap.visitSignature(signature);
        }
        return new RemapMethodVisitor(remap, super.visitMethod(access, name, descriptor, signature, exceptions));
    }

}
