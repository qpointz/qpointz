package io.qpointz.rapids.filesystem;


import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;

public class LocalFileSystemAdapter implements FileSystemAdapter {

    private final Path traverseRoot;

    public LocalFileSystemAdapter(Path traverseRoot) {
        this.traverseRoot = traverseRoot;
    }

    @Override
    public FileSystem getFileSytem() {
        return FileSystems.getDefault();
    }

    @Override
    public Path getTraverseRoot() {
        return this.traverseRoot;
    }

    @Override
    public void close() throws IOException {
        //based on default filesystem there is no resources to be closed
    }
}
