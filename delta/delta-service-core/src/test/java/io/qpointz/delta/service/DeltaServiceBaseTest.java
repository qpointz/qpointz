package io.qpointz.delta.service;

import io.qpointz.delta.proto.*;
import io.substrait.proto.Type;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DeltaServiceBaseTest {

    @Test
    void hanhshakeTest() {
        val deltaService = Mockito.mock(DeltaServiceBase.class, Answers.CALLS_REAL_METHODS);
        val resp = deltaService.onHandshake(HandshakeRequest.newBuilder().build());
        assertEquals(ResponseCode.OK, resp.getStatus().getCode());
    }

    @Test
    void listSchemas() {
        val exp = List.of("A","B","C");
        val schemaService = Mockito.mock(SchemaProvider.class, Answers.RETURNS_MOCKS);
        Mockito.when(schemaService.getSchemaNames()).thenReturn(exp);
        val deltaService = new DeltaServiceBase(schemaService, null, null);

        val resp = deltaService.onListSchemas(ListSchemasRequest.newBuilder().build());
        val s = resp.getSchemasList().stream().toList();
        assertEquals(s, exp);
        assertEquals(ResponseCode.OK, resp.getStatus().getCode());
    }

    @Test
    void getMissingSchema() {
        val schemaService = Mockito.mock(SchemaProvider.class, Answers.RETURNS_MOCKS);
        Mockito.when(schemaService.getSchema("A")).thenReturn(Optional.empty());
        val deltaService = new DeltaServiceBase(schemaService, null, null);

        val resp = deltaService.onGetSchema(GetSchemaRequest.newBuilder().build().newBuilder().setSchemaName("A").build());
        assertEquals(ResponseCode.INVALID_REQUEST, resp.getStatus().getCode());
    }

    @Test
    void getSchema() {
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

        val schemaService = Mockito.mock(SchemaProvider.class, Answers.RETURNS_MOCKS);
        Mockito.when(schemaService.getSchema(schemaName)).thenReturn(Optional.of(schema));
        val deltaService = new DeltaServiceBase(schemaService, null, null);

        val resp = deltaService.onGetSchema(GetSchemaRequest.newBuilder().setSchemaName("A").build());
        assertEquals(ResponseCode.OK, resp.getStatus().getCode());
        assertEquals(schema, resp.getSchema());
    }



}