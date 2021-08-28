package dialight.easynms.mapping.io;

import dialight.easynms.mapping.ClassVisitor;
import dialight.easynms.mapping.Mapping;
import dialight.easynms.mapping.MappingVisitor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class AnotherMappingFormatReader implements AutoCloseable {

    private final MappingVisitor visitor;
    private ClassVisitor currentClass = null;

    public AnotherMappingFormatReader(MappingVisitor visitor) {
        this.visitor = visitor;
    }

    public void accept(String line) {
        line = line.stripLeading();
        String[] split = line.split(" ");
        for (int i = 0; i < split.length; i++) {
            if(split[i].equals("null")) split[i] = null;
        }
        final var type = split[0];
        switch (type) {
            case "CL:": {
                if(currentClass != null) currentClass.visitEnd();
                currentClass = visitor.visitClass(split[1], split[2]);
                currentClass.visitStart();
            } break;
            case "FD:": {
                currentClass.visitField(split[1], split[2], split[3], split[4]);
            } break;
            case "MD:": {
                currentClass.visitMethod(split[1], split[2], split[3], split[4]);
            } break;
            default: throw new IllegalStateException("Unknown record type \"" + type + "\"");
        }
    }

    @Override
    public void close() {
        if(currentClass != null) currentClass.visitEnd();
    }

    public static Mapping read(Path file) throws IOException {
        Mapping mapping = new Mapping();
        mapping.visitStart();
        try(final var reader = Files.newBufferedReader(file)) {
            try(final var parser = new AnotherMappingFormatReader(mapping)) {
                reader.lines().forEach(parser::accept);
            }
        }
        mapping.visitEnd();
        return mapping;
    }

}
