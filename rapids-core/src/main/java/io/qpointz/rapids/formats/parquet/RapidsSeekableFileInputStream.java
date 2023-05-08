package io.qpointz.rapids.formats.parquet;

import org.apache.parquet.io.DelegatingSeekableInputStream;

import java.io.FileInputStream;
import java.io.IOException;

public class RapidsSeekableFileInputStream extends DelegatingSeekableInputStream {

    private final FileInputStream fileStream;

    public RapidsSeekableFileInputStream(FileInputStream fis) {
        super(fis);
        this.fileStream = fis;
    }


    @Override
    public long getPos() throws IOException {
        return this.fileStream.getChannel().position();
    }

    @Override
    public void seek(long newPos) throws IOException {
        this.fileStream.getChannel().position(newPos);
    }
}
