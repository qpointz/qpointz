package io.qpointz.rapids.formats.parquet;

import org.apache.calcite.linq4j.AbstractEnumerable;
import org.apache.calcite.linq4j.Enumerator;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Stream;

public abstract class FileEnumerator<TRec> extends AbstractEnumerable<Object[]> {

    private final Collection<Path> paths;

    protected FileEnumerator(Collection<Path> paths) {
        this.paths = paths;
    }

    public Stream<TRec> createRecordIterator() {
        return this.paths.stream()
                .flatMap(this::createRecordIterator);
    }

    public Stream<Object[]> createRowStream() {
        return this.createRecordIterator()
                .map(this::mapRecord);
    }

    protected abstract Stream<TRec> createRecordIterator(Path k);

    abstract Object[] mapRecord(TRec record);

    @Override
    public Enumerator<Object[]> enumerator() {
        return new Enumerator<Object[]>() {

            private Object[] current = null;
            private Iterator<Object[]> iter = this.resetIterator();

            private Iterator<Object[]> resetIterator() {
                this.current = null;
                return FileEnumerator.this.createRowStream().iterator();
            }

            @Override
            public Object[] current() {
                if (this.current==null) {
                    throw new IllegalStateException();
                }
                return this.current;
            }

            @Override
            public boolean moveNext() {
                this.current = null;
                var hasNext = this.iter.hasNext();
                if (hasNext) {
                    this.current = this.iter.next();
                }
                return hasNext;
            }

            @Override
            public void reset() {
                this.resetIterator();
            }

            @Override
            public void close() {
                // no close logic required
            }
        };
    }
}
