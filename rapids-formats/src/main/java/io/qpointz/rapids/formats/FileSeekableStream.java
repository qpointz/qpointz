package io.qpointz.rapids.formats;

import java.io.FileInputStream;
import java.io.IOException;

public class FileSeekableStream extends SeekableStream {

    private final FileInputStream fileStream;

    public FileSeekableStream(FileInputStream inputStream) {
        this.fileStream = inputStream;
    }

    @Override
    public long getPos() throws IOException {
        return this.fileStream.getChannel().position();
    }

    @Override
    public void seek(long newPos) throws IOException {
        this.fileStream.getChannel().position(newPos);
    }

    @Override
    public int read() throws IOException {
        return this.fileStream.read();
    }
}
