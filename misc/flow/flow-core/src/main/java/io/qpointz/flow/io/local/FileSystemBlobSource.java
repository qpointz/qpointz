package io.qpointz.flow.io.local;

import io.qpointz.flow.io.BlobPath;
import io.qpointz.flow.io.BlobSource;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.StreamSupport;

@AllArgsConstructor
public class FileSystemBlobSource implements BlobSource {

    @Getter
    private final Path rootPath;

    @Override
    public Collection<BlobPath> listBlobs() throws Exception {
        var absPath = this.rootPath
                .toAbsolutePath()
                .normalize();
        return Files.walk(absPath)
                .filter(k-> Files.isRegularFile(k) && !Files.isDirectory(k))
                .map(k-> (BlobPath) FileSystemBlobPath.of(absPath, k))
                .toList();

    }
}
