package dialight.easynms.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.options.Option;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public abstract class Setup extends DefaultTask {

    public Setup() {
    }

    //    private String url;
//
//    @Option(option = "url", description = "Configures the URL to be verified.")
//    public void setUrl(String url) {
//        this.url = url;
//    }
//
//    @Input
//    public String getUrl() {
//        return url;
//    }

    @InputFile
    abstract public RegularFileProperty getInputJar();

    @OutputFile
    abstract public RegularFileProperty getOutputJar();

    @TaskAction
    public void action() throws IOException {
        final var inputJar = getInputJar().get().getAsFile().toPath();
        final var outputJar = getOutputJar().get().getAsFile().toPath();

        Files.copy(inputJar, outputJar, StandardCopyOption.REPLACE_EXISTING);
    }

}
