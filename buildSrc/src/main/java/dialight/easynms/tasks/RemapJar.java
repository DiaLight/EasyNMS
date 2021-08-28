package dialight.easynms.tasks;

import dialight.easynms.EasyAsmRemapper;
import dialight.easynms.EasyRemapper;
import dialight.easynms.mapping.Mapping;
import dialight.easynms.mapping.io.AnotherMappingFormatReader;
import dialight.easynms.remap.RemapClassVisitor;
import dialight.easynms.tasks.remapjar.InheritGraph;
import dialight.easynms.tasks.remapjar.RemapClassItem;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.ClassRemapper;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;
import java.util.zip.ZipOutputStream;

public abstract class RemapJar extends DefaultTask {

    @InputFile
    abstract public RegularFileProperty getInputJar();

    @InputFile
    abstract public RegularFileProperty getInputMapping();

    @OutputFile
    abstract public RegularFileProperty getOutputJar();

    @TaskAction
    public void action() throws IOException {
        final var inJar = getInputJar().get().getAsFile().toPath();
        final var inMapping = getInputMapping().get().getAsFile().toPath();
        final var outJar = getOutputJar().get().getAsFile().toPath();

//        final var tempFile = Files.createTempFile("easynms-", ".jar");

        final var mapping = new Mapping();

        try (final var br = Files.newBufferedReader(inMapping);
             final var reader = new AnotherMappingFormatReader(mapping)) {
            br.lines().forEach(reader::accept);
        }

        final var tempFile = Files.createTempFile("easynms-", ".jar");
        try {
            // format new file as empty jar
            try(final var ignored = new ZipOutputStream(Files.newOutputStream(tempFile))) {}

            try (FileSystem readFs = FileSystems.newFileSystem(inJar, (ClassLoader) null);
                 FileSystem writeFs = FileSystems.newFileSystem(tempFile, (ClassLoader) null)) {
                final var writeRoot = writeFs.getPath("/");
                final var classFiles = new ArrayList<RemapClassItem>();

                final ConcurrentLinkedQueue<String> unvisited = new ConcurrentLinkedQueue<>();
                for (Path root : readFs.getRootDirectories()) {
                    Files.walkFileTree(root, new SimpleFileVisitor<>() {
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                            final var relativePath = root.relativize(file);
                            var outFile = writeRoot.resolve(relativePath);
                            final var classPath = relativePath.toString();

                            if(classPath.endsWith(".class")) {
                                final var className = classPath.substring(0, classPath.length() - ".class".length());
                                final var clazz = mapping.getClass(className);
                                if(clazz != null) outFile = writeRoot.resolve(clazz.dst + ".class");
//                                else unvisited.add("classFile " + className);
                                final var outDir = outFile.getParent();
                                if(Files.notExists(outDir)) Files.createDirectories(outDir);
                                classFiles.add(new RemapClassItem(file, outFile, clazz));
                            } else {
                                final var outDir = outFile.getParent();
                                if(Files.notExists(outDir)) Files.createDirectories(outDir);
                                Files.copy(file, outFile);
                            }
                            return FileVisitResult.CONTINUE;
                        }
                    });
                }
                final var inheritGraph = InheritGraph.build(classFiles);
//                inheritGraph.remap(mapping);
//                final var asmRemapper = new EasyAsmRemapper(mapping, inheritGraph);
                final var remapper = new EasyRemapper(mapping, inheritGraph);

                classFiles.parallelStream().forEach(remapClassItem -> {
                    final Function<ClassVisitor, ClassVisitor> modifier =
                            visitor -> new RemapClassVisitor(remapper, visitor, unvisited);
//                    final Function<ClassVisitor, ClassVisitor> modifier =
//                            visitor -> new ClassRemapper(visitor, asmRemapper);
                    try {
                        remapClassFile(modifier, remapClassItem.readFile, remapClassItem.writeFile);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
                try(BufferedWriter writer = Files.newBufferedWriter(Paths.get("F:\\workspace\\EasyNMS\\build\\easynms\\mappings\\diff.txt"))) {
                    for (String line : remapper.unmappedMethods.stream().sorted().distinct().toList()) {
                        writer.write(line);
                        writer.newLine();
                    }
                }
                try(BufferedWriter writer = Files.newBufferedWriter(Paths.get("F:\\workspace\\EasyNMS\\build\\easynms\\mappings\\unvisited.txt"))) {
                    for (String line : unvisited.stream().sorted().distinct().toList()) {
                        writer.write(line);
                        writer.newLine();
                    }
                }
                try(BufferedWriter writer = Files.newBufferedWriter(Paths.get("F:\\workspace\\EasyNMS\\build\\easynms\\mappings\\inherit.txt"))) {
                    for (InheritGraph.Pair pair : inheritGraph.getPairs().sorted().distinct().toList()) {
                        writer.write(pair.first + " extends " + pair.second);
                        writer.newLine();
                    }
                }
            }
            Files.copy(tempFile, outJar, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.delete(tempFile);
        }
    }

    private void remapClassFile(Function<ClassVisitor, ClassVisitor> modifier, Path readFile, Path writeFile) throws IOException {
        try (final var is = Files.newInputStream(readFile);
             final var os = Files.newOutputStream(writeFile)) {
            final var reader = new ClassReader(is);
            final var writer = new ClassWriter(reader, 0
//                    | ClassWriter.COMPUTE_FRAMES
                    | ClassWriter.COMPUTE_MAXS
            );
            reader.accept(modifier.apply(writer), 0
//                    | ClassReader.SKIP_CODE
//                    | ClassReader.SKIP_DEBUG
//                    | ClassReader.SKIP_FRAMES
                    | ClassReader.EXPAND_FRAMES
//                    | ClassReader.EXPAND_ASM_INSNS
            );
            os.write(writer.toByteArray());
        }
    }

}
