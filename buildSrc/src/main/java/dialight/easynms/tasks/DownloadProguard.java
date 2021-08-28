package dialight.easynms.tasks;

import dialight.easynms.utils.CopyFileVisitor;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;

public abstract class DownloadProguard extends DefaultTask {

    private static final String PROGUARD_URL = "https://github.com/Guardsquare/proguard/releases/download/v7.1.1/proguard-7.1.1.zip";

    public DownloadProguard() {
        getOutputs().upToDateWhen(task1 -> Files.exists(getProguardHome().get().getAsFile().toPath()));
    }

    @OutputDirectory
    abstract public RegularFileProperty getProguardHome();

    @OutputFile
    abstract public RegularFileProperty getProguardJar();

    @TaskAction
    public void action() throws IOException {
        final var proguardHome = getProguardHome().get().getAsFile().toPath();
        if (Files.notExists(proguardHome)) Files.createDirectories(proguardHome);

        final var proGuardZip = proguardHome.resolve("ProGuard.zip");
        if(Files.exists(proGuardZip)) {
            setDidWork(false);
            return;
        }

        try (BufferedInputStream in = new BufferedInputStream(new URL(PROGUARD_URL).openStream())) {
            try(OutputStream os = Files.newOutputStream(proGuardZip)) {
                in.transferTo(os);
            }
        }


        try (FileSystem zipfs = FileSystems.newFileSystem(proGuardZip, (ClassLoader) null)) {
            final var root = zipfs.getPath("/");
            final var files = new ArrayList<Path>();
            for (Path path : Files.newDirectoryStream(root)) files.add(path);
            if(files.size() != 1) throw new IllegalStateException("ProGuard.zip. Unexpected content");
            final var zipProguardHome = files.get(0);
            CopyFileVisitor.copyRecursive(zipProguardHome, proguardHome);
        }

    }

}
