package io.qpointz.mill.metadata.database;

import io.qpointz.mill.MillConnection;
import io.qpointz.mill.metadata.ResultSetProvidingMetadata;
import io.qpointz.mill.proto.ListSchemasRequest;
import io.qpointz.mill.types.logical.StringLogical;
import io.qpointz.mill.vectors.ObjectToVectorProducer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;

import java.util.Collection;
import java.util.List;

import static io.qpointz.mill.metadata.database.MetadataUtils.*;
import static io.qpointz.mill.vectors.ObjectToVectorProducer.mapper;

@AllArgsConstructor
public class SchemasMetadata extends ResultSetProvidingMetadata<SchemasMetadata.SchemaRecord> {

    @Getter
    private final MillConnection connection;

    protected record SchemaRecord(String catalog, String schema) {}

    private static List<ObjectToVectorProducer.MapperInfo<SchemasMetadata.SchemaRecord,?>> MAPPINGS = List.of(
            mapper("TABLE_CATALOG", StringLogical.INSTANCE, k-> dbnull()),
            mapper("TABLE_SCHEM", StringLogical.INSTANCE, k-> stringOf(k.schema))
    );

    @Override
    protected List<ObjectToVectorProducer.MapperInfo<SchemaRecord, ?>> getMappers() {
        return MAPPINGS;
    }

    @Override
    protected Collection<SchemaRecord> getMetadata() {
        val schemas = this.getConnection().getClient().newBlockingStub()
                .listSchemas(ListSchemasRequest.newBuilder().build());
        return schemas.getSchemasList().stream()
                .map(k-> new SchemaRecord(null, k))
                .toList();
    }


}
