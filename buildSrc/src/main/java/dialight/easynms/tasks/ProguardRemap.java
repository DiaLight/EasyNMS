package dialight.easynms.tasks;

import dialight.easynms.EasyNMSExtension;
import dialight.easynms.utils.ProcessUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public abstract class ProguardRemap extends DefaultTask {

    private final EasyNMSExtension ext = getProject().getExtensions().getByType(EasyNMSExtension.class);

    public ProguardRemap() {
        getOutputs().upToDateWhen(task -> Files.exists(getProguardJar().get().getAsFile().toPath()));
    }

    @InputFile
    abstract public RegularFileProperty getProguardJar();

    @InputFile
    abstract public RegularFileProperty getInputJar();

    @InputFile
    abstract public RegularFileProperty getInputMapping();

    @OutputFile
    abstract public RegularFileProperty getOutputJar();

    private String buildConfig(Path inputJar, Path outputJar, Path mapping) {
        StringBuilder sb = new StringBuilder();

        List<Path> injars = Arrays.asList(inputJar);
        sb.append("-injars ").append(injars.stream()
                .map(Path::toAbsolutePath)
                .map(Path::toString)
                .collect(Collectors.joining(";")));
        sb.append(System.lineSeparator());

        List<Path> outjars = Arrays.asList(outputJar);
        sb.append("-outjars ").append(outjars.stream()
                .map(Path::toAbsolutePath)
                .map(Path::toString)
                .collect(Collectors.joining(";")));
        sb.append(System.lineSeparator());

//        FileSystem fs = FileSystems.getFileSystem(URI.create("jrt:/"));
//        Path objClassFilePath = fs.getPath("modules", "java.base", "java/lang/Object.class");
        sb.append("-libraryjars <java.home>/jmods/java.base.jmod(!**.jar;!module-info.class)").append(System.lineSeparator());
        List<Path> libjars = Arrays.asList(
//                Paths.get(System.getProperty("java.home"), "../jre/lib/rt.jar"),
//                Paths.get(System.getProperty("java.home"), "../jre/lib/jce.jar")
                // TODO: minecraft libs
        );
        for (Path libjar : libjars) {
            sb.append("-libraryjars ")
                    .append('"')
                    .append(libjar.toAbsolutePath())
                    .append('"');
            sb.append(System.lineSeparator());
        }

        sb.append("-applymapping ").append(mapping.toAbsolutePath()).append(System.lineSeparator());

//        sb.append("-classobfuscationdictionary ").append(clsobfdict.getAbsolutePath()).append(System.lineSeparator());  // "input-obfdict.txt"

//        sb.append("-printmapping ").append(outMapping.getAbsolutePath()).append(System.lineSeparator());  // ".pgmap"

//        sb.append("-printconfiguration ").append(outConfig.getAbsolutePath()).append(System.lineSeparator());  // ".pro"

//        sb.append("-printseeds ").append(outSeeds.getAbsolutePath()).append(System.lineSeparator());  // ".keep"

//        sb.append("-printusage ").append(outUsage.getAbsolutePath()).append(System.lineSeparator());  // ".usage"

//        sb.append("-dump ").append(outDump.getAbsolutePath()).append(System.lineSeparator());

        sb.append(System.lineSeparator());

        sb.append("-ignorewarnings\n");
        sb.append(System.lineSeparator());

//        sb.append("-dontobfuscate\n");
        sb.append("-dontoptimize\n");
        sb.append("-dontshrink\n");
        sb.append("-allowaccessmodification\n");
        sb.append("-keepattributes *Annotation*\n");
//        sb.append("-keepattributes Exceptions\n");  // Don't need it
        sb.append("-keepattributes InnerClasses\n");
        sb.append("-keepattributes Signature\n");
        sb.append("-keepattributes SourceFile,LineNumberTable,EnclosingMethod\n");

//        sb.append("-keepclassmembers class **\n");
//        sb.append(
//                "-keepclassmembers enum ** {\n" +
//                        "    public static **[] values();\n" +
//                        "    public static ** valueOf(java.lang.String);\n" +
//                        "}\n"
//        );

        sb.append("-keep class !net.minecraft.** { *; }");
        sb.append(System.lineSeparator());

        return sb.toString();
    }

    @TaskAction
    public void action() throws IOException, InterruptedException {
        final var proguardJar = getProguardJar().get().getAsFile().toPath();
        final var inputJar = getInputJar().get().getAsFile().toPath();
        final var mapping = getInputMapping().get().getAsFile().toPath();
        final var outputJar = getOutputJar().get().getAsFile().toPath();

        final var tmpOutJar = outputJar.getParent().resolve(
                outputJar.getFileName().toString().substring(0, ".jar".length()) + ".tmp.jar"
        );

        java.lang.String configStr = buildConfig(inputJar, tmpOutJar, mapping);

        try(BufferedWriter bw = Files.newBufferedWriter(ext.getEasyNMSDir().resolve("config.pro"))) {
            bw.write(configStr);
        }

        final var builder = new ProcessBuilder();
        builder.command(System.getProperty("java.home") + "/bin/java", "-jar", proguardJar.toString(), "@config.pro");
        builder.directory(ext.getEasyNMSDir().toFile());

        Process proc = builder.start();
        ProcessUtils.handleIO(proc);
        Files.move(tmpOutJar, outputJar, StandardCopyOption.REPLACE_EXISTING);
    }

}
