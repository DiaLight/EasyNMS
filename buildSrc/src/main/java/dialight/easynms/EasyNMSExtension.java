package dialight.easynms;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Nested;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

abstract public class EasyNMSExtension {

    private final Path easyNMSDir;
    private final Path mappingsDir;
    private final Path remapDir;
    private final Path remappedJar;

    public EasyNMSExtension(Project project) throws IOException {
        easyNMSDir = project.getBuildDir().toPath().resolve("easynms");
        mappingsDir = easyNMSDir.resolve("mappings");
        remapDir = easyNMSDir.resolve("remap");
        remappedJar = easyNMSDir.resolve("remapped-server.jar");

        if (Files.notExists(easyNMSDir)) Files.createDirectories(easyNMSDir);
        if (Files.notExists(mappingsDir)) Files.createDirectories(mappingsDir);
        if (Files.notExists(remapDir)) Files.createDirectories(remapDir);

        project.getDependencies().add("implementation", project.files(remappedJar));
    }

    abstract public RegularFileProperty getOutputDir();

    abstract public Property<File> getMinecraftServerDir();
    abstract public Property<File> getRemappedMinecraftServer();
    abstract public Property<String> getMinecraftVersion();

    @Nested
    abstract public CustomData getCustomData();

    public void customData(Action<? super CustomData> action) {
        action.execute(getCustomData());
    }

    public Path getEasyNMSDir() {
        return easyNMSDir;
    }

    public Path getMappingsDir() {
        return mappingsDir;
    }

    public Path getRemapDir() {
        return remapDir;
    }

    public Path getRemappedJar() {
        return remappedJar;
    }

}