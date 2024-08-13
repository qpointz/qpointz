package io.qpointz.mill.metadata;

import io.qpointz.mill.MillConnection;
import io.qpointz.mill.types.logical.StringLogical;
import io.qpointz.mill.vectors.ObjectToVectorProducer;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static io.qpointz.mill.metadata.MetadataUtils.*;
import static io.qpointz.mill.vectors.ObjectToVectorProducer.mapper;

public class CatalogsMetadata {

    private record CatalogRecord(String catalog) {};

    private static List<ObjectToVectorProducer.MapperInfo<CatalogsMetadata.CatalogRecord,?>> MAPPINGS = List.of(
            mapper("TABLE_CAT", StringLogical.INSTANCE, k-> dbnull())
    );

    public static ResultSet asResultSet(MillConnection connection) throws SQLException {
        return ObjectToVectorProducer.resultSet(CatalogsMetadata.MAPPINGS, List.of());
    }
}
