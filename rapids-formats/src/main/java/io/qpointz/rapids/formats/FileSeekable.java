package io.qpointz.rapids.formats;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class FileSeekable extends SeekableFile {

    private final File file;

    public FileSeekable(File file) {
        this.file = file;
    }

    @Override
    public long getLength() throws IOException {
        return this.file.length();
    }

    @Override
    public SeekableStream newStream() throws IOException {
        return new FileSeekableStream(new FileInputStream(this.file));
    }
}
