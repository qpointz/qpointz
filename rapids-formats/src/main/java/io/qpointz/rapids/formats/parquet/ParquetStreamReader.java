package io.qpointz.rapids.formats.parquet;

import io.qpointz.rapids.formats.DataReader;
import io.qpointz.rapids.formats.SeekableFile;
import io.qpointz.rapids.formats.SeekableStream;
import org.apache.avro.generic.GenericRecord;
import org.apache.parquet.avro.AvroParquetReader;
import org.apache.parquet.hadoop.ParquetReader;
import org.apache.parquet.io.DelegatingSeekableInputStream;
import org.apache.parquet.io.InputFile;
import org.apache.parquet.io.SeekableInputStream;
import java.io.IOException;

public abstract class ParquetStreamReader<T> implements DataReader<T> {

    private final InputFileImpl inputFile;
    private final ParquetReader<GenericRecord> reader;

    protected class DelegatingSeekableInputStreamImpl extends DelegatingSeekableInputStream {

        private final SeekableStream seekable;

        public DelegatingSeekableInputStreamImpl(SeekableStream seekable) {
            super(seekable);
            this.seekable = seekable;
        }
        @Override
        public long getPos() throws IOException {
            return this.seekable.getPos();
        }

        @Override
        public void seek(long newPos) throws IOException {
            this.seekable.seek(newPos);
        }
    }
    protected class InputFileImpl implements InputFile {

        private final SeekableFile seekableFile;

        public InputFileImpl(SeekableFile file) {
            this.seekableFile = file;
        }

        @Override
        public long getLength() throws IOException {
            return this.seekableFile.getLength();
        }

        @Override
        public SeekableInputStream newStream() throws IOException {
            final var seekable = this.seekableFile.newStream();
            return new DelegatingSeekableInputStreamImpl(seekable);
        }
    }

    public ParquetStreamReader(SeekableFile inputFile) throws IOException {
        this.inputFile = new InputFileImpl(inputFile);
        this.reader = null; // AvroParquetReader.genericRecordReader(this.inputFile);
    }

    protected abstract T asT(GenericRecord record);

    @Override
    public T read() throws IOException {
        final var record = this.reader.read();
        if (record==null) {
            return null;
        }

        return asT(record);
    }

    @Override
    public void close() {
        try {
            this.reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}