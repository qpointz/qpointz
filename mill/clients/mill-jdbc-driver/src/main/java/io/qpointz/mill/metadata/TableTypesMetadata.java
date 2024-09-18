package io.qpointz.mill.metadata;

import io.qpointz.mill.MillConnection;
import io.qpointz.mill.types.logical.StringLogical;
import io.qpointz.mill.vectors.ObjectToVectorProducer;
import lombok.val;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import static io.qpointz.mill.metadata.MetadataUtils.stringOf;
import static io.qpointz.mill.vectors.ObjectToVectorProducer.mapper;

public class TableTypesMetadata {

    private static List<ObjectToVectorProducer.MapperInfo<String,?>> MAPPINGS = List.of(
            mapper("TABLE_TYPE", StringLogical.INSTANCE, k-> stringOf(k))
    );

    public static ResultSet asResultSet(MillConnection connection) throws SQLException {
        val tableTypes = List.of("TABLE", "VIEW");
        return ObjectToVectorProducer.resultSet(TableTypesMetadata.MAPPINGS, tableTypes);


    }


}
