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

public abstract class InvertClassMappings extends DefaultTask {

    @InputFile
    abstract public Property<Path> getInputClassMap();

    @OutputFile
    abstract public RegularFileProperty getOutputClassMap();

    @TaskAction
    public void action() throws IOException {
        final var inClassMapFile = getInputClassMap().get();
        final var outClassMapFile = getOutputClassMap().get().getAsFile().toPath();

        try(final var writer = Files.newBufferedWriter(outClassMapFile)) {
            try(BufferedReader br = Files.newBufferedReader(inClassMapFile)) {
                br.lines()
                        .filter(line -> !line.startsWith("#"))
                        .map(line -> line.split(" "))
                        .forEach(cols -> {
                            try {
                                writer.write("%s %s\n".formatted(cols[1], cols[0]));
                            } catch (IOException e) { throw new RuntimeException(e); }
                        });
            }
        }
    }

}
