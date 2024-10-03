package io.qpointz.mill.services;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.qpointz.mill.proto.*;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class MillServiceMetadataTest extends MillServiceBaseTest {


    @Test
    void handshakeTest(@Autowired MillServiceGrpc.MillServiceBlockingStub stub) {
        val on = stub.handshake(HandshakeRequest.getDefaultInstance());
        assertTrue(on.getCapabilities().getSupportSql());
        assertEquals(ProtocolVersion.V1_0, on.getVersion());
    }

    @Test
    void listSchemas(@Autowired MillServiceGrpc.MillServiceBlockingStub stub,
                     @Autowired MetadataProvider metadataProvider) {
        val exp = List.of("A", "B", "C");
        when(metadataProvider.getSchemaNames()).thenReturn(exp);
        val resp = stub.listSchemas(ListSchemasRequest.newBuilder().build());
        val s = resp.getSchemasList().stream().toList();
        assertEquals(s, exp);
    }

    @Test
    void getMissingSchema(@Autowired MillServiceGrpc.MillServiceBlockingStub stub,
                          @Autowired MetadataProvider metadataProvider) {
        when(metadataProvider.isSchemaExists(any())).thenReturn(false);
        val request = GetSchemaRequest.getDefaultInstance();
        val e = assertThrows(StatusRuntimeException.class,
                () -> stub.getSchema(request));
        val status = e.getStatus();
        assertEquals(Status.Code.NOT_FOUND, status.getCode());
    }

    @Test
    void getSchema(@Autowired MillServiceGrpc.MillServiceBlockingStub stub,
                   @Autowired MetadataProvider metadataProvider) {
        val schema = Schema.newBuilder().build();
        when(metadataProvider.getSchema(any())).thenReturn(schema);
        when(metadataProvider.isSchemaExists(any())).thenReturn(true);
        val request = GetSchemaRequest.getDefaultInstance();
        val resp = stub.getSchema(request);
        assertEquals(schema, resp.getSchema());
    }
}