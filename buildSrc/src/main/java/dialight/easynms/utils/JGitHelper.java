package dialight.easynms.utils;

import dialight.easynms.builddata.BDInfo;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public class JGitHelper {

    @Nullable
    public static <T> T readText(Repository repo, RevCommit commit, String path, Function<BufferedReader, T> consumer) throws IOException {
        final var tree = commit.getTree();
        try (TreeWalk treeWalk = new TreeWalk(repo)) {
            treeWalk.addTree(tree.getId());
            treeWalk.setRecursive(true);
//            treeWalk.setFilter(PathFilter.create(path));
            while (treeWalk.next()) {
                if(!Objects.equals(treeWalk.getPathString(), path)) continue;
                ObjectLoader loader = repo.open(treeWalk.getObjectId(0));
                try(BufferedReader r = new BufferedReader(new InputStreamReader(loader.openStream(), StandardCharsets.UTF_8))) {
                    return consumer.apply(r);
                }
            }
        }
        return null;
    }

    @Nullable
    public static <T> T readText(Repository repo, String path, Function<BufferedReader, T> consumer) throws IOException {
        ObjectId lastCommitId = repo.resolve(Constants.HEAD);
        try (RevWalk revWalk = new RevWalk(repo)) {
            RevCommit commit = revWalk.parseCommit(lastCommitId);
            return readText(repo, commit, path, consumer);
        }
    }

    public static RevTree getTree(Repository repository) throws IOException {
        ObjectId lastCommitId = repository.resolve(Constants.HEAD);
        // a RevWalk allows to walk over commits based on some filtering
        try (RevWalk revWalk = new RevWalk(repository)) {
            RevCommit commit = revWalk.parseCommit(lastCommitId);
//            System.out.println("Time of commit (seconds since epoch): " + commit.getCommitTime());
            return commit.getTree();
        }
    }

    private static void printFile(Repository repository, RevTree tree, String path) throws IOException {
        // now try to find a specific file
        try (TreeWalk treeWalk = new TreeWalk(repository)) {
            treeWalk.addTree(tree.getId());
            treeWalk.setRecursive(false);
            treeWalk.setFilter(PathFilter.create("README.md"));
            if (!treeWalk.next()) {
                throw new IllegalStateException("Did not find expected file '" + path + "'");
            }

            // FileMode specifies the type of file, FileMode.REGULAR_FILE for normal file, FileMode.EXECUTABLE_FILE for executable bit
            // set
            FileMode fileMode = treeWalk.getFileMode(0);
            ObjectLoader loader = repository.open(treeWalk.getObjectId(0));
            System.out.println(path + ": " + getFileMode(fileMode) + ", type: " + fileMode.getObjectType() + ", mode: " + fileMode +
                    " size: " + loader.getSize());
        }
    }

    private static String getFileMode(FileMode fileMode) {
        if (fileMode.equals(FileMode.EXECUTABLE_FILE)) {
            return "Executable File";
        } else if (fileMode.equals(FileMode.REGULAR_FILE)) {
            return "Normal File";
        } else if (fileMode.equals(FileMode.TREE)) {
            return "Directory";
        } else if (fileMode.equals(FileMode.SYMLINK)) {
            return "Symlink";
        } else {
            // there are a few others, see FileMode javadoc for details
            throw new IllegalArgumentException("Unknown type of file encountered: " + fileMode);
        }
    }

}
