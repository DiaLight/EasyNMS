package dialight.easynms.tasks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dialight.easynms.EasyNMSExtension;
import dialight.easynms.builddata.BDInfo;
import dialight.easynms.utils.JGitHelper;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

public abstract class DownloadBuildData extends DefaultTask {

    private static final String BUILD_DATA_URL = "https://hub.spigotmc.org/stash/scm/spigot/builddata.git";
    private static final Pattern RE_COMMIT = Pattern.compile("Update to Minecraft (.*)");
    private static final Gson GSON = new GsonBuilder()
            .create();

    private final EasyNMSExtension ext = getProject().getExtensions().getByType(EasyNMSExtension.class);

    public DownloadBuildData() {
        this.getOutputs().upToDateWhen(element -> {
            return false;
        });
    }

    @OutputDirectory
    abstract public RegularFileProperty getOutputRepoDir();

    public final Property<Path> outputClassMappings = getProject().getObjects().property(Path.class).convention((Path) null);

    public final Property<Path> outputMemberMappings = getProject().getObjects().property(Path.class).convention((Path) null);

    public final Property<String> memberMapCommand = getProject().getObjects().property(String.class).convention((String) null);

    public final Property<String> classMapCommand = getProject().getObjects().property(String.class).convention((String) null);

    public final Property<String> mappingsUrl = getProject().getObjects().property(String.class).convention((String) null);

    @TaskAction
    public void action() throws IOException, GitAPIException {
        final var buildDataDir = getOutputRepoDir().get().getAsFile().toPath();
        final var projectDir = getProject().getProjectDir().toPath();

        final Git git;
        if (Files.notExists(buildDataDir)) {
            System.out.println(projectDir.relativize(buildDataDir).toString()
                    + " not exist. Copy repo from "
                    + BUILD_DATA_URL
            );
            Files.createDirectories(buildDataDir);
            git = Git.cloneRepository()
                    .setURI(BUILD_DATA_URL)
                    .setDirectory(buildDataDir.toFile())
                    .call();
        } else {
            git = Git.open(buildDataDir.toFile());
        }
        final var repo = git.getRepository();

        for (RevCommit commit : git.log().all().call()) {
            final var matcher = RE_COMMIT.matcher(commit.getShortMessage());
            if(!matcher.matches()) continue;
            final BDInfo info = JGitHelper.readText(repo, commit, "info.json", reader -> GSON.fromJson(reader, BDInfo.class));
            if(info == null) continue;
            if(!ext.getMinecraftVersion().get().equals(info.minecraftVersion)) {
//                System.out.println("skip " + info.minecraftVersion);
                continue;
            }
//            RevCommit latestCommit = new Git(repository).log().setMaxCount(1).call().iterator().next();
//            String latestCommitHash = latestCommit.getName();
            RevCommit head;
            try (RevWalk walk = new RevWalk(repo)) {
                head = walk.parseCommit(repo.resolve("HEAD"));
                walk.dispose();
            }
            updateInfo(buildDataDir, info);
            if(!AnyObjectId.isEqual(head.getId(), commit.getId())) {
                System.out.println("Switch commit from " + head.getId().getName().substring(0, 7) + " " + head.getShortMessage());
                System.out.println("  to " + commit.getId().getName().substring(0, 7) + " " + commit.getShortMessage());

                git.checkout().setName(commit.getId().getName()).call();
                this.setDidWork(true);
            }
            this.setDidWork(false);
            return;
        }
        throw new IllegalStateException("minecraft remapped server jar not found in cache");
    }

    private void updateInfo(Path buildDataDir, BDInfo info) {
        outputClassMappings.set(buildDataDir.resolve("mappings").resolve(info.classMappings));
        outputMemberMappings.set(buildDataDir.resolve("mappings").resolve(info.memberMappings));
        memberMapCommand.set(info.memberMapCommand);
        classMapCommand.set(info.classMapCommand);
        mappingsUrl.set(info.mappingsUrl);
    }

}
