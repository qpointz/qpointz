package io.qpointz.rapids.formats;

import java.io.IOException;

public abstract class SeekableFile {

    public abstract long getLength() throws IOException;

    public abstract SeekableStream newStream() throws IOException;

}

