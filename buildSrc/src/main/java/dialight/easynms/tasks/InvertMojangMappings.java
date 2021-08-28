package dialight.easynms.tasks;

import dialight.easynms.mapping.Mapping;
import dialight.easynms.mapping.io.ProguardReader;
import dialight.easynms.mapping.io.ProguardWriter;
import dialight.easynms.mapping.utils.Mappings;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;

public abstract class InvertMojangMappings extends DefaultTask {

    @InputFile
    abstract public RegularFileProperty getInputMapping();

    @OutputFile
    abstract public RegularFileProperty getOutputMapping();

    @TaskAction
    public void action() throws IOException {
        final var inMapping = getInputMapping().get().getAsFile().toPath();
        final var outMapping = getOutputMapping().get().getAsFile().toPath();

        Mapping mapping = new Mapping();

        mapping.visitStart();
        try(final var reader = Files.newBufferedReader(inMapping)) {
            try(final var parser = new ProguardReader(mapping)) {
                reader.lines().forEach(parser::accept);
            }
        }
        mapping.visitEnd();

        Mappings.fixMissingDstDesc(mapping);
        mapping = Mappings.inverseCopy(mapping);
        ProguardWriter.write(outMapping, mapping);
    }

}
