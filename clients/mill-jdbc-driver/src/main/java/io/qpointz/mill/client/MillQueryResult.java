package io.qpointz.mill.client;

import io.qpointz.mill.proto.QueryResultResponse;
import io.qpointz.mill.proto.VectorBlock;
import io.qpointz.mill.proto.VectorBlockSchema;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Transport-agnostic query result abstraction that exposes stable schema metadata
 * and a single-pass stream of data vector blocks.
 *
 * The returned iterator is thread-safe for concurrent hasNext/next calls.
 */
public class MillQueryResult {

    private final VectorBlockSchema schema;
    private final Iterator<VectorBlock> vectorBlocks;

    private MillQueryResult(VectorBlockSchema schema, Iterator<VectorBlock> vectorBlocks) {
        this.schema = schema;
        this.vectorBlocks = vectorBlocks;
    }

    public static MillQueryResult fromResponses(Iterator<QueryResultResponse> responses) {
        VectorBlock firstVector = null;
        while (responses.hasNext() && firstVector == null) {
            QueryResultResponse response = responses.next();
            if (response.hasVector()) {
                firstVector = response.getVector();
            }
        }

        VectorBlockSchema schema = firstVector != null && firstVector.hasSchema()
                ? firstVector.getSchema()
                : null;

        Iterator<VectorBlock> vectors = new ResponseVectorIterator(firstVector, responses);
        return new MillQueryResult(schema, vectors);
    }

    public boolean hasSchema() {
        return this.schema != null;
    }

    public VectorBlockSchema getSchema() {
        if (this.schema == null) {
            throw new NoSuchElementException("No schema available for query result");
        }
        return this.schema;
    }

    public Iterator<VectorBlock> getVectorBlocks() {
        return this.vectorBlocks;
    }

    private static final class ResponseVectorIterator implements Iterator<VectorBlock> {
        private final Iterator<QueryResultResponse> responses;
        private VectorBlock nextVector;

        private ResponseVectorIterator(VectorBlock firstVector, Iterator<QueryResultResponse> responses) {
            this.responses = responses;
            this.nextVector = firstVector;
        }

        @Override
        public synchronized boolean hasNext() {
            if (this.nextVector != null) {
                return true;
            }
            while (this.responses.hasNext()) {
                QueryResultResponse response = this.responses.next();
                if (response.hasVector()) {
                    this.nextVector = response.getVector();
                    return true;
                }
            }
            return false;
        }

        @Override
        public synchronized VectorBlock next() {
            if (!this.hasNext()) {
                throw new NoSuchElementException("No more vector blocks");
            }
            VectorBlock current = this.nextVector;
            this.nextVector = null;
            return current;
        }
    }
}
