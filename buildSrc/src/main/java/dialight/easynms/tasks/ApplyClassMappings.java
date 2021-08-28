package dialight.easynms.tasks;

import dialight.easynms.utils.ProcessUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.Task;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.MessageFormat;

public abstract class ApplyClassMappings extends DefaultTask {

    @InputDirectory
    abstract public RegularFileProperty getInputRepoDir();

    @Input
    abstract public Property<String> getInputMapCommand();

    @InputFile
    abstract public RegularFileProperty getInputJar();

    @InputFile
    abstract public RegularFileProperty getInputClassMap();

    @OutputFile
    abstract public RegularFileProperty getOutputJar();

    @TaskAction
    public void action() throws IOException {
        final var repoDir = getInputRepoDir().get().getAsFile().toPath();
        final var inJar = getInputJar().get().getAsFile().toPath();
        final var classMap = getInputClassMap().get().getAsFile().toPath();
        final var outJar = getOutputJar().get().getAsFile().toPath();

        final var tmpOutJar = outJar.getParent().resolve(
                outJar.getFileName().toString().substring(0, ".jar".length()) + ".tmp.jar"
        );
        ProcessUtils.run(repoDir.getParent(), MessageFormat.format(System.getProperty("java.home") + "/bin/" + MessageFormat.format(
                getInputMapCommand().get(),
                inJar.toString(),
                classMap.toString(),
                "{0}"
        ), tmpOutJar.toString()).split(" "));
        Files.move(tmpOutJar, outJar, StandardCopyOption.REPLACE_EXISTING);
    }

}
