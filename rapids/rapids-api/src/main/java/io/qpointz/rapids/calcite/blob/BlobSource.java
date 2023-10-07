package io.qpointz.rapids.calcite.blob;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.util.stream.Stream;

public interface BlobSource extends Closeable {

    String getSchema();

    SeekableByteChannel openSeekableChannel(BlobPath path) throws IOException;

    ReadableByteChannel openReadableChannel(BlobPath path) throws IOException;

    Stream<BlobPath> listBlobs() throws IOException;

}
