package io.qpointz.mill.metadata;

import io.qpointz.mill.MillConnection;
import io.qpointz.mill.proto.ListSchemasRequest;
import io.qpointz.mill.types.logical.StringLogical;
import io.qpointz.mill.vectors.ObjectToVectorProducer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static io.qpointz.mill.metadata.MetadataUtils.*;
import static io.qpointz.mill.vectors.ObjectToVectorProducer.mapper;

@AllArgsConstructor
public class SchemasMetadata {

    @Getter
    private final MillConnection connection;

    public static ResultSet asResultSet(MillConnection connection) throws SQLException {
        return new SchemasMetadata(connection).asResultSet();
    }

    private record SchemaRecord(String catalog, String schema) {
    }

    private static List<ObjectToVectorProducer.MapperInfo<SchemasMetadata.SchemaRecord,?>> MAPPINGS = List.of(
            mapper("TABLE_CATALOG", StringLogical.INSTANCE, k-> dbnull()),
            mapper("TABLE_SCHEM", StringLogical.INSTANCE, k-> stringOf(k.schema))
    );

    private ResultSet asResultSet() throws SQLException {
        val schemas = this.getConnection().createClient().newBlockingStub()
                .listSchemas(ListSchemasRequest.newBuilder().build());
        val records = schemas.getSchemasList().stream()
                .map(k-> new SchemaRecord(null, k))
                .toList();
        return ObjectToVectorProducer.resultSet(SchemasMetadata.MAPPINGS, records);

    }
}
