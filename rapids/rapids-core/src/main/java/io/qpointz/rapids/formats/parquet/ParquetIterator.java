package io.qpointz.rapids.formats.parquet;

import org.apache.avro.generic.GenericRecord;
import org.apache.parquet.hadoop.ParquetReader;

import java.io.IOException;
import java.util.Iterator;
import java.util.Optional;

public class ParquetIterator implements Iterable<GenericRecord> {

    private final ParquetReader<GenericRecord> reader;

    public ParquetIterator(ParquetReader<GenericRecord> reader) {
        this.reader = reader;
    }

    @Override
    public Iterator<GenericRecord> iterator() {
        return new Iterator<GenericRecord>() {

            private GenericRecord current = null;
            private Optional<Boolean> state = Optional.empty();

            @Override
            public boolean hasNext() {
                if (state.isEmpty()) {
                    current();
                }
                return state.get();
            }

            @Override
            public GenericRecord next() {
                var next = current();
                this.state = Optional.empty();
                return next;
            }

            private GenericRecord current() {
                if (state.isPresent()) {
                    return this.current;
                } else {
                    try {
                        this.current = reader.read();
                        this.state = Optional.of(this.current!=null);
                        return this.current;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        };
    }


}
