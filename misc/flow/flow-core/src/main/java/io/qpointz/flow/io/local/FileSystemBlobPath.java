package io.qpointz.flow.io.local;

import io.qpointz.flow.io.Blob;
import io.qpointz.flow.io.BlobPath;
import lombok.val;

import java.net.URI;
import java.nio.file.Path;

public record FileSystemBlobPath(Path rootPath, Path location) implements BlobPath {

    @Override
    public URI getUri() {
        return location.toUri();
    }

    @Override
    public Blob blob() {
        return new FileSystemBlob(this);
    }

    public static FileSystemBlobPath of(Path rootPath, Path absolutePth) {
        val normalized = rootPath.normalize();
        return new FileSystemBlobPath(normalized,
                normalized.relativize(absolutePth).normalize());
    }
}
