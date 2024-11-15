package io.qpointz.mill.sql;

import io.qpointz.mill.proto.VectorBlock;
import io.qpointz.mill.vectors.VectorBlockIterator;
import java.util.Iterator;

public class RecordReaders {

    private static class VectorBlockIteratorRecordReaderImp extends VectorBlockRecordIterator {

        protected VectorBlockIteratorRecordReaderImp(Iterator<VectorBlock> vectorBlocks) {
            super(vectorBlocks);
        }

        @Override
        public void close() {

        }
    }

    public static RecordReader recordReader(VectorBlockIterator iterator) {
        return new VectorBlockIteratorRecordReaderImp(iterator);
    }

}
