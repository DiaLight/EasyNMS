package dialight.easynms.mapping.utils;

import dialight.easynms.mapping.*;
import dialight.easynms.mapping.tools.Dumper;
import dialight.easynms.mapping.tools.InvertProxy;
import dialight.easynms.mapping.utils.CharIterator;

import java.util.Arrays;
import java.util.Iterator;
import java.util.function.Function;

public class Mappings {

    public static final IgnoreClass IGNORE_CLASS = new IgnoreClass();

    public static class IgnoreClass implements ClassVisitor {
        private IgnoreClass() {}
        @Override public void visitField(String src, String dst, String srcDesc, String dstDesc) {}
        @Override public void visitMethod(String src, String dst, String srcDesc, String dstDesc) {}
    }

    public static void dump(Mapping mapping) {
        final var dumper = new Dumper(System.out::println);
        dumper.visitStart();
        mapping.accept(dumper);
        dumper.visitEnd();
    }


    public static Mapping copy(Mapping mapping) {
        Mapping copy = new Mapping();
        copy.visitStart();
        mapping.accept(copy);
        copy.visitEnd();
        return copy;
    }

    public static Mapping inverseCopy(Mapping mapping) {
        Mapping copy = new Mapping();
        copy.visitStart();
        mapping.accept(new InvertProxy(copy));
        copy.visitEnd();
        return copy;
    }

    private static void remapReference(Iterator<Character> in, StringBuilder out, Function<String, String> remapRef) {
        StringBuilder sb = new StringBuilder();
        while (true) {
            if(!in.hasNext()) throw new IllegalStateException("bad reference");
            char c = in.next();
            if(c == ';') break;
            sb.append(c);
        }
        String src = sb.toString();
        src = remapRef.apply(src);
        if(src == null || src.length() == 0) throw new IllegalStateException("remap failed " + in);
        out.append(src);
        out.append(';');
    }

    public static String remapDesc(String desc, Function<String, String> remapRef) {
        CharIterator in = new CharIterator(desc);
        StringBuilder out = new StringBuilder(desc.length());
        if(!in.hasNext()) throw new IllegalStateException("empty desc");
        while(true) {
            char c = in.next();out.append(c);
            if(c == '(') {  // args
                while(in.hasNext()) {
                    c = in.next();out.append(c);
                    if(c == ')') break;
                    if(c == 'L') remapReference(in, out, remapRef);
                }
                if(!in.hasNext()) throw new IllegalStateException("empty ret");
                c = in.next();out.append(c);
            }
            if(c == 'L') remapReference(in, out, remapRef);
            if(!in.hasNext()) break;
        }
        return out.toString();
    }
    public static String remapDesc(Mapping mapping, String desc) {
        return remapDesc(desc, src -> {
            Clazz clazz = mapping.getClass(src);
            if(clazz != null) {
                src = clazz.dst;
            } else {
                int $idx = src.indexOf('$');
                if($idx != -1) {  // inner class
                    clazz = mapping.getClass(src.substring(0, $idx));
                    if(clazz != null) {
                        src = clazz.dst + src.substring($idx);
                    }
                }
            }
            return src;
        });
    }

    private static void remapSignatureGeneric(Mapping mapping, Iterator<Character> in, StringBuilder out) {
        char c;
        while (true) {
            if(!in.hasNext()) throw new IllegalStateException("bad reference");
            c = in.next();
            out.append(c);
            if(c == 'L') remapSignatureReference(mapping, in, out);
            if(c == '<') remapSignatureGeneric(mapping, in, out);
            if(c == '>') break;
        }
    }
    private static void remapSignatureReference(Mapping mapping, Iterator<Character> in, StringBuilder out) {
        StringBuilder sb = new StringBuilder();
        char c;
        while (true) {
            if(!in.hasNext()) throw new IllegalStateException("bad reference");
            c = in.next();
            if(c == '<' || c == ';') break;
            sb.append(c);
        }
        String src = sb.toString();
        Clazz clazz = mapping.getClass(src);
        if(clazz != null) {
            out.append(clazz.dst);
        } else {
            out.append(src);
        }
        out.append(c);
        if(c == '<') {
            remapSignatureGeneric(mapping, in, out);
            if(!in.hasNext()) throw new IllegalStateException("empty desc" + in);
            c = in.next();out.append(c);
            if(c != ';') throw new IllegalStateException("unexpected char " + in);
        }
    }
    public static String remapSignature(Mapping mapping, String signature) {
        CharIterator in = new CharIterator(signature);
        StringBuilder out = new StringBuilder(signature.length());
        if(!in.hasNext()) throw new IllegalStateException("empty signature");
        final var VALID_CHARS = Arrays.asList('(', ')', '[', 'V', 'T', 'K', 'J', 'I', 'F', 'E', 'Z', 'D', 'A', 'B', 'C', ';');
        while(true) {
            char c = in.next();out.append(c);
            if(!VALID_CHARS.contains(c)) {
                if(c == 'L') remapSignatureReference(mapping, in, out);
                else if(c == '<') remapSignatureGeneric(mapping, in, out);
                else throw new IllegalStateException("invalid signature " + in);
            }
            if(!in.hasNext()) break;
        }
        return out.toString();
    }

    public static void fixMissingDstDesc(Mapping mapping) {
        for (Clazz clazz : mapping.getClasses()) {
            for (Member member : clazz.getMembers()) {
                final var srcDesc = member.getSrcDesc();
                final var dstDesc = member.getDstDesc();
                if (srcDesc == null) continue;  // fields may not have srcDesc
                if (dstDesc != null) continue;  // ignore exist dstDesc
                member.setDstDesc(remapDesc(mapping, srcDesc));
            }
        }
    }

    public static void addMissingMembers(Mapping dstMapping, Mapping srcMapping) {
        for (Clazz dstClass : dstMapping.getClasses()) {
            Clazz srcClass = srcMapping.getClass(dstClass.src);
            if(srcClass == null) continue;
            for (Member srcMember : srcClass.getMembers()) {
                Member dstMember = dstClass.getMember(srcMember.getSrcId());
                if(dstMember != null) continue;

                Member newMember = srcMember.copy();  // keyToKey
                String dstDesc = srcMember.getSrcDesc();
                if (dstDesc != null) newMember.setDstDesc(remapDesc(dstMapping, dstDesc));  // map key.dstDesc -> dst.dstDesc

                dstClass.addMember(newMember);
            }
        }
    }

    public static void applyToDst(Mapping mapping, Mapping modifier) {
        for (Clazz clazz : mapping.getClasses()) {  // this.srcToKey
            Clazz modifierClass = modifier.getClass(clazz.dst);
            if(modifierClass == null) {
                int $idx = clazz.dst.indexOf('$');
                if($idx != -1) {  // inner class
                    modifierClass = modifier.getClass(clazz.dst.substring(0, $idx));
                    if(modifierClass != null) {
                        clazz.dst = modifierClass.dst + clazz.dst.substring($idx);
                    }
                }
                modifierClass = null;
            } else {
                clazz.dst = modifierClass.dst;
            }
            for (Member member : clazz.getMembers()) {  // this.srcToKey
                Member modifierMember = modifierClass != null ? modifierClass.getMember(member.getDstId()) : null;
                if (modifierMember != null) {
                    member.dst = modifierMember.dst;
                    final var modifierDstDesc = modifierMember.getDstDesc();
                    if(modifierDstDesc != null) {
                        member.setDstDesc(modifierDstDesc);
                        continue;
                    }
                }
                String dstDesc = member.getDstDesc();
                if (dstDesc == null) dstDesc = remapDesc(mapping, member.getSrcDesc());
                member.setDstDesc(remapDesc(modifier, dstDesc));
            }
        }
    }

}
