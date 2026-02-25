package io.qpointz.mill.client;

import io.qpointz.mill.MillCodeException;
import io.qpointz.mill.proto.*;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class MillClientAsyncTest {

    @Test
    void handshakeAsyncShouldReturnResponse() throws Exception {
        var client = new FakeMillClient(false);
        var response = client.handshakeAsync(HandshakeRequest.getDefaultInstance())
                .get(5, TimeUnit.SECONDS);
        assertNotNull(response);
    }

    @Test
    void handshakeAsyncShouldPropagateFailure() {
        var client = new FakeMillClient(true);
        var thrown = assertThrows(ExecutionException.class, () ->
                client.handshakeAsync(HandshakeRequest.getDefaultInstance()).get(5, TimeUnit.SECONDS));
        assertNotNull(thrown.getCause());
        assertTrue(thrown.getCause() instanceof MillCodeException);
        assertEquals("expected", thrown.getCause().getMessage());
    }

    @Test
    void execQueryAsyncShouldReturnIterator() throws Exception {
        var client = new FakeMillClient(false);
        var iterator = client.execQueryAsync(QueryRequest.getDefaultInstance())
                .get(5, TimeUnit.SECONDS);
        assertTrue(iterator.hasNext());
        assertNotNull(iterator.next());
    }

    private static class FakeMillClient extends MillClient {
        private final boolean fail;

        private FakeMillClient(boolean fail) {
            this.fail = fail;
        }

        @Override
        public String getClientUrl() {
            return "mem://test";
        }

        @Override
        public HandshakeResponse handshake(HandshakeRequest request) throws MillCodeException {
            if (fail) {
                throw new MillCodeException("expected");
            }
            return HandshakeResponse.getDefaultInstance();
        }

        @Override
        public ListSchemasResponse listSchemas(ListSchemasRequest request) {
            return ListSchemasResponse.getDefaultInstance();
        }

        @Override
        public GetSchemaResponse getSchema(GetSchemaRequest request) {
            return GetSchemaResponse.getDefaultInstance();
        }

        @Override
        public Iterator<QueryResultResponse> execQuery(QueryRequest request) {
            return List.of(QueryResultResponse.getDefaultInstance()).iterator();
        }

        @Override
        public void close() {
            // no-op
        }
    }
}
