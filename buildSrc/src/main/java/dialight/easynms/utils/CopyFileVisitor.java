package dialight.easynms.utils;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.function.Predicate;

public class CopyFileVisitor extends SimpleFileVisitor<Path> {

    private final Path src;
    private final Path dst;
    private final Predicate<String> filter;

    private CopyFileVisitor(Path src, Path dst, Predicate<String> filter) {
        this.src = src;
        this.dst = dst;
        this.filter = filter;
    }

    @Override public FileVisitResult visitFile(Path path, BasicFileAttributes basicFileAttributes) throws IOException {
        String relative = src.relativize(path).toString();
        if (!filter.test(relative)) return FileVisitResult.CONTINUE;
        Path out = dst.resolve(relative);
        Files.createDirectories(out.getParent());
        Files.copy(path, out, StandardCopyOption.REPLACE_EXISTING);
        return FileVisitResult.CONTINUE;
    }

    public static void copyRecursive(Path src, Path dst) throws IOException {
        copyRecursive(src, dst, s -> true);
    }

    public static void copyRecursive(Path src, Path dst, Predicate<String> filter) throws IOException {
        Files.walkFileTree(src, new CopyFileVisitor(src, dst, filter));
    }

}
