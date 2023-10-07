package io.qpointz.rapids.parcels.filesystem;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.function.Consumer;

@Slf4j
class FileSystemParcelTest {

    public void traverse(Path path, Consumer<Path> consumer) {
        this.traverse(path.toFile(), x -> consumer.accept(x.toPath()));
    }

    public void traverse(File f, Consumer<File> consumer) {
        consumer.accept(f);
        if (f.isFile()) {
            return;
        }
        for (var tf : f.listFiles()) {
            traverse(tf, consumer);
        }
    }

    @Test
    void traverseFileSystem() {
        var fs = FileSystems.getDefault();
        var path = Path.of("../");
        var ps = fs.getPath(String.valueOf(path.toAbsolutePath()));
        traverse(ps, p-> System.out.println(p));
    }




}