package io.qpointz.flow.io.local;

import io.qpointz.flow.io.Blob;
import io.qpointz.flow.io.BlobPath;
import lombok.val;

import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public record FileSystemBlob(FileSystemBlobPath path) implements Blob {
    @Override
    public BlobPath getPath() {
        return path();
    }

    @Override
    public ReadableByteChannel openReadableChannel() throws IOException {
        return openSeekableChannel();
    }

    @Override
    public SeekableByteChannel openSeekableChannel() throws IOException {
        val path = Paths.get(path().getUri());
        try (val inputStream = Files.newInputStream(path, StandardOpenOption.READ)) {
            return (SeekableByteChannel) Channels.newChannel(inputStream);
        }
    }


}
