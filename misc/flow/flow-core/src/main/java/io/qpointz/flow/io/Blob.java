package io.qpointz.flow.io;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;

public interface Blob {
    BlobPath getPath();
    ReadableByteChannel openReadableChannel() throws IOException;
    SeekableByteChannel openSeekableChannel() throws IOException;
}
