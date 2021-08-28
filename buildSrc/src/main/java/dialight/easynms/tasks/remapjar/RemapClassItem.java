package dialight.easynms.tasks.remapjar;

import dialight.easynms.mapping.Clazz;

import java.nio.file.Path;

public class RemapClassItem {

    public final Path readFile;
    public final Path writeFile;
    public final Clazz clazz;

    public RemapClassItem(Path readFile, Path writeFile, Clazz clazz) {
        this.readFile = readFile;
        this.writeFile = writeFile;
        this.clazz = clazz;
    }

}
