package dialight.easynms.mapping.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dialight.easynms.mapping.Mapping;
import dialight.easynms.mapping.utils.CharIterator;
import dialight.easynms.mapping.ClassVisitor;
import dialight.easynms.mapping.MappingVisitor;

public class ProguardReader implements AutoCloseable {

    private static final Pattern RE_MEMBER = Pattern.compile("([\\d:]+)?((\\S+) (([\\w.]*)\\.)?([<>$\\w]+)(\\(\\S*\\))?)([\\d:]+)? -> (\\S+)");
    private final MappingVisitor visitor;
    private ClassVisitor currentClass = null;

    public ProguardReader(MappingVisitor visitor) {
        this.visitor = visitor;
    }

    public void accept(String line) {
        if (line.startsWith("#")) return;
        if(line.startsWith("    ")) {
            line = line.substring(4);
            Matcher m = RE_MEMBER.matcher(line);
            if(!m.matches()) {
                throw new NullPointerException(line);
            }
            String srcDesc = m.group(2);  // java type
            String src = m.group(6);
            String args = m.group(7);
            String dst = m.group(9);
            if(src.equals("<init>")) {
                if(!dst.equals("<init>")) throw new RuntimeException("constructor mapping? " + line);
                return;
            }
            if(src.equals("<clinit>")) {
                if(!dst.equals("<clinit>")) throw new RuntimeException("constant constructor mapping? " + line);
                return;
            }
            srcDesc = toNative(srcDesc);
            if(args != null) {  // method
                currentClass.visitMethod(src, dst, srcDesc, null);
            } else {  // field
                currentClass.visitField(src, dst, srcDesc, null);
            }
        } else {
            if(currentClass != null) currentClass.visitEnd();
            String[] split = line.split(" -> ", 2);
            String src = split[0].replace('.', '/');
            String dst = split[1].replace('.', '/');
            dst = dst.substring(0, dst.length() - 1);
            currentClass = visitor.visitClass(src, dst);
            currentClass.visitStart();
        }
    }

    public static String readFirstType(CharIterator in) {
        StringBuilder out = new StringBuilder();
        char c;
        while(in.hasNext()) {
            c = in.next();
            if(c == ' ') break;
            out.append(c);
        }
        return out.toString();
    }
    public static String readMethodArg(CharIterator in) {
        StringBuilder out = new StringBuilder();
        char c;
        while(true) {
            if(!in.hasNext()) throw new IllegalStateException("bad reference");
            c = in.next();
            if(c == ',') break;
            if(c == ')') break;
            out.append(c);
        }
        return out.toString();
    }

    public static String toNative(String desc) {
        CharIterator in = new CharIterator(desc);
        if(!in.hasNext()) throw new IllegalStateException("empty desc");
        String firstType = readFirstType(in);
        // skip until args
        boolean isMethod = false;
        while(in.hasNext()) {
            char c = in.next();
            if(c == '(') {
                isMethod = true;
                break;
            }
        }
        if(!isMethod) return mapTypeToNative(firstType);
        StringBuilder out = new StringBuilder();
        out.append('(');
        while(in.hasNext()) {
            String arg = readMethodArg(in);
            if(arg.isEmpty()) break;
            out.append(mapTypeToNative(arg));
        }
        out.append(')');
        out.append(mapTypeToNative(firstType));
        return out.toString();
    }

    public static String mapTypeToNative(String type) {
        int idx = type.indexOf('[');
        String prefix = "";
        if(idx != -1) {
            int endType = idx;
            StringBuilder sb = new StringBuilder();
            sb.append('[');
            while((idx = type.indexOf('[', idx + 1)) != -1) sb.append('[');
            type = type.substring(0, endType);
            prefix = sb.toString();
        }
        if(type.equals("byte")) return prefix + "B";
        if(type.equals("char")) return prefix + "C";
        if(type.equals("double")) return prefix + "D";
        if(type.equals("float")) return prefix + "F";
        if(type.equals("int")) return prefix + "I";
        if(type.equals("long")) return prefix + "J";
        if(type.equals("short")) return prefix + "S";
        if(type.equals("boolean")) return prefix + "Z";
        if(type.equals("void")) return prefix + "V";
        return prefix + "L" + type.replace(".", "/") + ";";
    }

    @Override
    public void close() {
        if(currentClass != null) currentClass.visitEnd();
    }

    public static Mapping read(Path file) throws IOException {
        Mapping mapping = new Mapping();
        mapping.visitStart();
        try(final var reader = Files.newBufferedReader(file)) {
            try(final var parser = new ProguardReader(mapping)) {
                reader.lines().forEach(parser::accept);
            }
        }
        mapping.visitEnd();
        return mapping;
    }

}
