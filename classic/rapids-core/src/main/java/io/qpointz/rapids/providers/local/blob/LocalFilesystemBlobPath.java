package io.qpointz.rapids.providers.local.blob;

import io.qpointz.rapids.calcite.blob.BlobPath;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.nio.file.Path;

@AllArgsConstructor
public class LocalFilesystemBlobPath implements BlobPath {

    @Getter
    private final String schema;

    @Getter
    private final String path;

    @Getter
    private final Path rootPath;

}
