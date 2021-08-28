package dialight.easynms.tasks;

import dialight.easynms.mapping.Mapping;
import dialight.easynms.mapping.Clazz;
import dialight.easynms.mapping.io.ProguardReader;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public abstract class FixRemapDuplicates extends DefaultTask {

    @InputFile
    abstract public RegularFileProperty getInputMapping();

    @InputFile
    abstract public RegularFileProperty getInputJar();

    @OutputFile
    abstract public RegularFileProperty getOutputJar();

    @TaskAction
    public void action() throws IOException {
        final var inMapping = getInputMapping().get().getAsFile().toPath();
        final var inJar = getInputJar().get().getAsFile().toPath();
        final var outJar = getOutputJar().get().getAsFile().toPath();

        Mapping mapping = ProguardReader.read(inMapping);

        final var collisionMapping = new HashMap<String, Clazz>();
        for (Clazz clazz : mapping.getClasses()) {
            if(!clazz.src.contains("$")) continue;
            if(!clazz.dst.contains("$")) continue;
            final var srcSubClass = clazz.src.substring(clazz.src.lastIndexOf("$") + 1);
            final var srcClass = clazz.src.substring(0, clazz.src.lastIndexOf("$"));
            final var dstSubClass = clazz.dst.substring(clazz.dst.lastIndexOf("$") + 1);
            final var dstClass = clazz.dst.substring(0, clazz.dst.lastIndexOf("$"));
            collisionMapping.put(srcClass + "$" + dstSubClass, clazz);
        }
        final Predicate<String> filterDuplicateClass = (String name) -> {
            if (!name.endsWith(".class")) return true;
            if (name.contains("/")) return true;
            name = name.substring(0, name.length() - ".class".length());
            if (!name.contains("$")) return true;

            final var aClass = mapping.getClass(name);
            if (aClass != null) return true;
            final var clazz = collisionMapping.get(name);
            if (clazz == null) return true;
            return false;
        };
        try(ZipInputStream zin = new ZipInputStream(new FileInputStream(inJar.toFile()))) {
            try(ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outJar.toFile()))) {
                ZipEntry entry = zin.getNextEntry();
                while (entry != null) {
                    if (filterDuplicateClass.test(entry.getName())) {
                        out.putNextEntry(new ZipEntry(entry.getName()));
                        zin.transferTo(out);
                    }
                    entry = zin.getNextEntry();
                }
            }
        }
    }

}
