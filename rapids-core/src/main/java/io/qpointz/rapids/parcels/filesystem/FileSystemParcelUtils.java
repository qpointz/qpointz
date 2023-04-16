package io.qpointz.rapids.parcels.filesystem;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import java.util.function.Consumer;

public class FileSystemParcelUtils {

    public static void traverse(Path path, Consumer<Path> consumer) throws FileNotFoundException {
        traverse(path.toFile(), x -> consumer.accept(x.toPath()));
}

    public static void traverse(File f, Consumer<File> consumer) throws FileNotFoundException {
        if (!f.exists()) {
            throw new FileNotFoundException("%s not found".formatted(f.toString()));
        }

        consumer.accept(f);
        if (f.isFile()) {
            return;
        }
        for (var tf : f.listFiles()) {
            traverse(tf, consumer);
        }
    }

}
