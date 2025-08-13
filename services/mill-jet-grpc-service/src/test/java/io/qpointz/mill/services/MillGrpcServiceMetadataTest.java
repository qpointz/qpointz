package io.qpointz.mill.services;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.qpointz.mill.proto.*;
import io.qpointz.mill.services.dispatchers.DataOperationDispatcher;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class MillGrpcServiceMetadataTest extends MillGrpcServiceBaseTest {


    @Test
    void handshakeTest(@Autowired DataConnectServiceGrpc.DataConnectServiceBlockingStub stub) {
        val on = stub.handshake(HandshakeRequest.getDefaultInstance());
        assertFalse(on.getCapabilities().getSupportSql());
        assertEquals(ProtocolVersion.V1_0, on.getVersion());
    }

    @Test
    void listSchemas(@Autowired DataConnectServiceGrpc.DataConnectServiceBlockingStub stub,
                     @Autowired SchemaProvider schemaProvider) {
        val exp = List.of("A", "B", "C");
        when(schemaProvider.getSchemaNames()).thenReturn(exp);
        val resp = stub.listSchemas(ListSchemasRequest.newBuilder().build());
        val s = resp.getSchemasList().stream().toList();
        assertEquals(s, exp);
    }

    @Test
    void getMissingSchema(@Autowired DataConnectServiceGrpc.DataConnectServiceBlockingStub stub,
                          @Autowired SchemaProvider schemaProvider) {
        when(schemaProvider.isSchemaExists(any())).thenReturn(false);
        val request = GetSchemaRequest.getDefaultInstance();
        val e = assertThrows(StatusRuntimeException.class,
                () -> stub.getSchema(request));
        val status = e.getStatus();
        assertEquals(Status.Code.NOT_FOUND, status.getCode());
    }

    @Test
    void getSchema(@Autowired DataConnectServiceGrpc.DataConnectServiceBlockingStub stub,
                   @Autowired DataOperationDispatcher dataOperationDispatcher,
                   @Autowired SchemaProvider schemaProvider) {
        val schema = Schema.newBuilder().build();
        val schemaName = "schemaName";
        val request = GetSchemaRequest.newBuilder().setSchemaName(schemaName).build();
        when(schemaProvider.isSchemaExists("schemaName")).thenReturn(true);
        when(schemaProvider.getSchema("schemaName")).thenReturn(schema);
        val resp = stub.getSchema(request);
        assertEquals(schema, resp.getSchema());
    }
}