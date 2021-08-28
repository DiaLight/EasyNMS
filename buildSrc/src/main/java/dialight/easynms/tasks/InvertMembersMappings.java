package dialight.easynms.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.Task;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class InvertMembersMappings extends DefaultTask {

    @InputFile
    abstract public Property<Path> getInputMemberMap();

    @OutputFile
    abstract public RegularFileProperty getOutputMemberMap();

    @TaskAction
    public void action() throws IOException {
        final var inMapFile = getInputMemberMap().get();
        final var outMapFile = getOutputMemberMap().get().getAsFile().toPath();
        if(Files.exists(outMapFile)) {
            setDidWork(false);
            return;
        }

        try(final var writer = Files.newBufferedWriter(outMapFile)) {
            try(BufferedReader br = Files.newBufferedReader(inMapFile)) {
                br.lines()
                        .filter(line -> !line.startsWith("#"))
                        .map(line -> line.split(" "))
                        .forEach(cols -> {
                            try {
                                writer.write("%s %s %s %s\n".formatted(cols[0], cols[3], cols[2], cols[1]));
                            } catch (IOException e) { throw new RuntimeException(e); }
                        });
            }
        }
    }

}
