package io.qpointz.rapids.providers.local.blob;

import io.qpointz.rapids.calcite.blob.BlobPath;
import io.qpointz.rapids.calcite.blob.BlobSource;

import java.io.File;
import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.*;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class LocalFilesystemBlobSource implements BlobSource {

    private final Path rootPath;

    public LocalFilesystemBlobSource(Path rootPath) {
        this.rootPath = rootPath.toAbsolutePath();
    }

    @Override
    public String getSchema() {
        return "file";
    }

    @Override
    public SeekableByteChannel openSeekableChannel(BlobPath path) throws IOException {
        return openFileChannelForRead(path);
    }

    @Override
    public ReadableByteChannel openReadableChannel(BlobPath path) throws IOException {
        return openFileChannelForRead(path);
    }

    private SeekableByteChannel openFileChannelForRead(BlobPath path) throws IOException {
        final var localPath = unwrapAndValidate(path);
        final var absPath = Paths.get(this.rootPath.toString(), localPath.getPath());
        return Files.newByteChannel(absPath, StandardOpenOption.READ);
    }

    private LocalFilesystemBlobPath unwrapAndValidate(BlobPath path) throws IOException {
        if (! (path instanceof LocalFilesystemBlobPath)) {
            throw new IOException("Not suported path type");
        }

        final var localPath = (LocalFilesystemBlobPath) path;

        if (! Files.isSameFile(this.rootPath, localPath.getRootPath())) {
            throw new IOException("Origin source mismatches to path origin. Operation not supported.");
        }

        return localPath;
    }

    @Override
    public Stream<BlobPath> listBlobs() throws IOException {
        var absPath = this.rootPath.toAbsolutePath();
        try (var directoryStream = Files.newDirectoryStream(this.rootPath)) {
            return StreamSupport.stream(directoryStream.spliterator(), false)
                    .filter(Files::isDirectory)
                    .map(k -> k.toFile().listFiles())
                    .filter(Objects::nonNull)
                    .flatMap(Arrays::stream)
                    .map(File::toPath)
                    .map(this.rootPath::relativize)
                    .map(k-> (BlobPath) new LocalFilesystemBlobPath(this.getSchema(), k.toString(), absPath))
                    .toList()
                    .stream();
        }
    }

    @Override
    public void close() throws IOException {
        //no closable resources used nothing to close
    }
}
