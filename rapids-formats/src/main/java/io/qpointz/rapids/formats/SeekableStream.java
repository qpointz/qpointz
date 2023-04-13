package io.qpointz.rapids.formats;

import java.io.IOException;
import java.io.InputStream;

public abstract class SeekableStream extends InputStream {

    public abstract long getPos() throws IOException;

    public abstract void seek(long newPos) throws IOException;
}