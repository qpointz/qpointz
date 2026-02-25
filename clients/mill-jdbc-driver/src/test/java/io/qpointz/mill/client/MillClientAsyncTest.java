package io.qpointz.mill.client;

import io.qpointz.mill.MillCodeException;
import io.qpointz.mill.proto.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class MillClientAsyncTest {

    @Test
    void handshakeAsyncShouldReturnResponse() throws Exception {
        try (var client = new FakeMillClient(false)) {
            var response = client.handshakeAsync(HandshakeRequest.getDefaultInstance())
                    .get(5, TimeUnit.SECONDS);
            assertNotNull(response);
        }
    }

    @Test
    void handshakeAsyncShouldPropagateFailure() {
        try (var client = new FakeMillClient(true)) {
            var thrown = assertThrows(ExecutionException.class, () ->
                    client.handshakeAsync(HandshakeRequest.getDefaultInstance()).get(5, TimeUnit.SECONDS));
            assertNotNull(thrown.getCause());
            assertTrue(thrown.getCause() instanceof MillCodeException);
            assertEquals("expected", thrown.getCause().getMessage());
        }
    }

    @Test
    void execQueryAsyncShouldReturnIterator() throws Exception {
        try (var client = new FakeMillClient(false)) {
            var result = client.execQueryAsync(QueryRequest.getDefaultInstance())
                    .get(5, TimeUnit.SECONDS);
            var iterator = result.getVectorBlocks();
            assertTrue(iterator.hasNext());
            assertNotNull(iterator.next());
        }
    }

    @Test
    void listSchemasAsyncShouldReturnResponse() throws Exception {
        try (var client = new FakeMillClient(false)) {
            var response = client.listSchemasAsync(ListSchemasRequest.getDefaultInstance())
                    .get(5, TimeUnit.SECONDS);
            assertNotNull(response);
        }
    }

    @Test
    void getSchemaAsyncShouldReturnResponse() throws Exception {
        try (var client = new FakeMillClient(false)) {
            var response = client.getSchemaAsync(GetSchemaRequest.getDefaultInstance())
                    .get(5, TimeUnit.SECONDS);
            assertNotNull(response);
        }
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
        public MillQueryResult execQuery(QueryRequest request) {
            var response = QueryResultResponse.newBuilder()
                    .setVector(VectorBlock.getDefaultInstance())
                    .build();
            return MillQueryResult.fromResponses(List.of(response).iterator());
        }

        @Override
        public void close() {
            // no-op
        }
    }
}
