package io.qpointz.rapids.filesystem;

import io.qpointz.rapids.formats.SeekableStream;

import java.io.IOException;

public class SeekableAlike extends SeekableStream {
    @Override
    public long getPos() throws IOException {
        return 0;
    }

    @Override
    public void seek(long newPos) throws IOException {

    }

    @Override
    public int read() throws IOException {
        return 0;
    }
}
