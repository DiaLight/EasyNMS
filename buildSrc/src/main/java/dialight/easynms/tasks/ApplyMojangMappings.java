package dialight.easynms.tasks;

import dialight.easynms.utils.ProcessUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.Task;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public abstract class ApplyMojangMappings extends DefaultTask {

    @InputDirectory
    abstract public RegularFileProperty getInputRepoDir();

    @InputFile
    abstract public RegularFileProperty getInputJar();

    @InputFile
    abstract public RegularFileProperty getInputMojangMap();

    @OutputFile
    abstract public RegularFileProperty getOutputJar();

    @TaskAction
    public void action() throws IOException {
        final var repoDir = getInputRepoDir().get().getAsFile().toPath();
        final var inJar = getInputJar().get().getAsFile().toPath();
        final var mojangMap = getInputMojangMap().get().getAsFile().toPath();
        final var outJar = getOutputJar().get().getAsFile().toPath();

        final var tmpOutJar = outJar.getParent().resolve(
                outJar.getFileName().toString().substring(0, ".jar".length()) + ".tmp.jar"
        );

        ProcessUtils.run(repoDir.getParent(),
                System.getProperty("java.home") + "/bin/java", "-jar", "BuildData/bin/SpecialSource-2.jar",
                "map",
                "--only", ".",
                "--only", "net/minecraft/server",
//                "--only", "com/mojang/math",
//                "-e", "BuildData/mappings/bukkit-1.17.1.exclude",
//                "-e", excludeFile.toString(),
                "-i", inJar.toString(),
                "-m", mojangMap.toString(),
                "-o", tmpOutJar.toString()
        );
        Files.move(tmpOutJar, outJar, StandardCopyOption.REPLACE_EXISTING);
    }

}
