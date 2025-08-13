package io.qpointz.rapids.formats.parquet;

import org.apache.parquet.io.InputFile;
import org.apache.parquet.io.SeekableInputStream;

import java.io.IOException;

public abstract class RapidsInputFile implements InputFile {
    @Override
    public long getLength() throws IOException {
        return 0;
    }

    @Override
    public SeekableInputStream newStream() throws IOException {
        return null;
    }
}
