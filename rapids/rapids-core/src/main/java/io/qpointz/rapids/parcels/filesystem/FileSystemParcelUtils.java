package io.qpointz.rapids.parcels.filesystem;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

public class FileSystemParcelUtils {

    public static void traverse(Path path, Consumer<Path> consumer) throws IOException {
        if (!Files.exists(path)) {
            throw new FileNotFoundException("%s not found".formatted(path.toString()));
        }
        consumer.accept(path);

        if (Files.isDirectory(path)) {
            for (var p: Files.newDirectoryStream(path)) {
                traverse(p, consumer);
            }
        }
    }

    /*private static void traverse(File f, Consumer<File> consumer) throws FileNotFoundException {
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
    }*/

}
