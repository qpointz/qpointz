package io.qpointz.rapids.calcite.schemas.blob;

import java.io.Closeable;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.util.stream.Stream;

public interface BlobSource extends Closeable {

    String getSchema();

    String getName();

    SeekableByteChannel openSeekableChannel(BlobPath path);

    ReadableByteChannel openReadableChannel(BlobPath path);

    Stream<BlobPath> listBlobs();

}
