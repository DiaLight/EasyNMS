package dialight.easynms.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class DownloadMojangMappings extends DefaultTask {

    public DownloadMojangMappings() {
        getOutputs().upToDateWhen(task1 -> Files.exists(getOutputMapping().get().getAsFile().toPath()));
    }

    @Input
    abstract public Property<String> getInputMappingsUrl();

    @OutputFile
    abstract public RegularFileProperty getOutputMapping();

    @TaskAction
    public void action() throws IOException {
        final var mojangMap = getOutputMapping().get().getAsFile().toPath();
        if(Files.exists(mojangMap)) {
            setDidWork(false);
            return;
        }

        try (BufferedInputStream in = new BufferedInputStream(new URL(getInputMappingsUrl().get()).openStream())) {
            try(OutputStream os = Files.newOutputStream(mojangMap)) {
                in.transferTo(os);
            }
        }
    }

}
