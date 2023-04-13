package io.qpointz.rapids.parcels.filesystem;

import java.io.File;
import java.nio.file.Path;
import java.util.function.Consumer;

class FileSystemParcelUtils {

    public static void traverse(Path path, Consumer<Path> consumer) {
        traverse(path.toFile(), x -> consumer.accept(x.toPath()));
    }

    public static void traverse(File f, Consumer<File> consumer) {
        consumer.accept(f);
        if (f.isFile()) {
            return;
        }
        for (var tf : f.listFiles()) {
            traverse(tf, consumer);
        }
    }

}
