package io.qpointz.delta.service;

import io.qpointz.delta.proto.*;
import io.substrait.proto.Type;
import lombok.val;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DeltaServiceMetadataTest {

    @Test
    public void handshakeTest() {
        val deltaService = Mockito.mock(DeltaService.class, Answers.CALLS_REAL_METHODS);

        try (val ctx = DeltaService.inProcess(deltaService)) {
            val on = ctx.blocking().handshake(HandshakeRequest.newBuilder().build());
            assertEquals(ResponseCode.OK, on.getStatus().getCode());
            assertEquals(false, on.getCapabilities().getSupportSql());
            assertEquals(ProtocolVersion.V1_0, on.getVersion());
        }
    }

    @Test
    @Disabled
    void handshakeUnhandledExceptionTest() {
        val deltaService = Mockito.mock(DeltaService.class, Answers.CALLS_REAL_METHODS);
        HandshakeRequest request = HandshakeRequest.newBuilder().build();
        Mockito.when(deltaService.onHandshake(request)).thenThrow(new RuntimeException("Server Error"));
        try (val ctx = DeltaService.inProcess(deltaService)) {
            val on = ctx.blocking().handshake(request);
            assertEquals(ResponseCode.ERROR_SERVER_ERROR, on.getStatus().getCode());
            assertEquals("Server Error", on.getStatus().getMessage());
        }
    }


    @Test
    void listSchemasTest() {
        val exp = List.of("A","B","C");
        val schemaService = Mockito.mock(MetadataProvider.class, Answers.RETURNS_MOCKS);
        Mockito.when(schemaService.getSchemaNames()).thenReturn(exp);
        val deltaService = new DeltaService(schemaService, null, null);

        try (val ctx = DeltaService.inProcess(deltaService)) {
            val resp = ctx.blocking().listSchemas(ListSchemasRequest.newBuilder().build());
            val s = resp.getSchemasList().stream().toList();
            assertEquals(s, exp);
            assertEquals(ResponseCode.OK, resp.getStatus().getCode());
        }
    }

    @Test
    void getSchemaWhenMissingTest() throws IOException {
        val schemaService = Mockito.mock(MetadataProvider.class, Answers.RETURNS_MOCKS);
        Mockito.when(schemaService.getSchema("A")).thenReturn(Optional.empty());
        val deltaService = new DeltaService(schemaService, null, null);
        try (val ctx = DeltaService.inProcess(deltaService)) {
            val resp = ctx.blocking().getSchema(GetSchemaRequest.newBuilder().build().newBuilder().setSchemaName("A").build());
            assertEquals(ResponseCode.ERROR_INVALID_REQUEST, resp.getStatus().getCode());
        }
    }

    @Test
    void getSchemaTest() {
        val schemaName = "A";
        val schema = Schema.newBuilder()
            .addAllTables(List.of(
            Table.newBuilder()
                    .setName("Table1")
                    .setSchemaName(schemaName)
                    .addAllFields(List.of(
                            Field.newBuilder()
                                    .setName("C1")
                                    .setType(Type.newBuilder()
                                            .setI8(Type.I8.newBuilder()
                                                    .setTypeVariationReference(0)
                                                    .setNullability(Type.Nullability.NULLABILITY_REQUIRED)
                                                    .build()
                                            )
                                            .build())
                                    .build(),
                            Field.newBuilder()
                                    .setName("C2")
                                    .setType(Type.newBuilder()
                                            .setString(Type.String.newBuilder()
                                                    .setTypeVariationReference(0)
                                                    .setNullability(Type.Nullability.NULLABILITY_REQUIRED)
                                                    .build()
                                            )
                                            .build())
                                    .build()
                    ))
                   .build()
        )).build();

        val schemaService = Mockito.mock(MetadataProvider.class, Answers.RETURNS_MOCKS);
        Mockito.when(schemaService.getSchema(schemaName)).thenReturn(Optional.of(schema));
        val deltaService = new DeltaService(schemaService, null, null);

        try (val ctx = DeltaService.inProcess(deltaService)) {
            val resp = ctx.blocking().getSchema(GetSchemaRequest.newBuilder().setSchemaName("A").build());
            assertEquals(ResponseCode.OK, resp.getStatus().getCode());
            assertEquals(schema, resp.getSchema());
        }
    }
}