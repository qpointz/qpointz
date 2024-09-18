package io.qpointz.mill.metadata;

import io.qpointz.mill.MillConnection;
import io.qpointz.mill.proto.DataType;
import io.qpointz.mill.proto.GetSchemaRequest;
import io.qpointz.mill.proto.ListSchemasRequest;
import io.qpointz.mill.proto.Table;
import io.qpointz.mill.sql.JdbcUtils;
import io.qpointz.mill.types.logical.IntLogical;
import io.qpointz.mill.types.logical.StringLogical;
import io.qpointz.mill.types.sql.DatabaseType;
import io.qpointz.mill.vectors.ObjectToVectorProducer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.java.Log;
import lombok.val;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import static io.qpointz.mill.metadata.MetadataUtils.*;
import static io.qpointz.mill.vectors.ObjectToVectorProducer.mapper;

@Log
@AllArgsConstructor
public class ColumnsMetadata {

    @Getter
    private final MillConnection connection;

    private record ColumnRecord(String catalog, String schema, String tableName, String name, Integer index, Integer dataType, String dataTypeName,
        Integer size, Integer decimalDigits,  Integer numPrecRadix, int nullable, String isNullable, int sizeBytes) {}

    private static List<ObjectToVectorProducer.MapperInfo<ColumnsMetadata.ColumnRecord,?>> MAPPINGS = List.of(
            mapper("TABLE_CAT", StringLogical.INSTANCE, k-> stringOf(k.catalog)),
            mapper("TABLE_SCHEM", StringLogical.INSTANCE, k-> stringOf(k.schema)),
            mapper("TABLE_NAME", StringLogical.INSTANCE, k-> stringOf(k.tableName)),
            mapper("COLUMN_NAME", StringLogical.INSTANCE, k-> stringOf(k.name)),
            mapper("DATA_TYPE", IntLogical.INSTANCE, k-> integerOf(k.dataType)),
            mapper("TYPE_NAME", StringLogical.INSTANCE, k-> stringOf(k.dataTypeName)),
            mapper("COLUMN_SIZE", IntLogical.INSTANCE, k-> integerOf(k.size, -1)),
            mapper("BUFFER_LENGTH", IntLogical.INSTANCE, k-> dbnull()),
            mapper("DECIMAL_DIGITS", IntLogical.INSTANCE, k-> integerOf(k.decimalDigits, -1)),
            mapper("NUM_PREC_RADIX", IntLogical.INSTANCE, k-> integerOf(k.numPrecRadix, -1)),
            mapper("NULLABLE", IntLogical.INSTANCE, k-> integerOf(k.nullable, -1)),
            mapper("REMARKS", StringLogical.INSTANCE, k-> dbnull()),
            mapper("COLUMN_DEF", StringLogical.INSTANCE, k-> dbnull()),
            mapper("SQL_DATA_TYPE", IntLogical.INSTANCE, k-> dbnull()),
            mapper("SQL_DATETIME_SUB", IntLogical.INSTANCE, k-> dbnull()),
            mapper("CHAR_OCTET_LENGTH", IntLogical.INSTANCE, k-> integerOf(k.sizeBytes, -1)),
            mapper("ORDINAL_POSITION", IntLogical.INSTANCE, k-> integerOf(k.index)),
            mapper("IS_NULLABLE", StringLogical.INSTANCE, k-> stringOf(k.isNullable)),
            mapper("SCOPE_CATALOG", StringLogical.INSTANCE, k-> dbnull()),
            mapper("SCOPE_SCHEMA", StringLogical.INSTANCE, k-> dbnull()),
            mapper("SCOPE_TABLE", StringLogical.INSTANCE, k-> dbnull()),
            mapper("SOURCE_DATA_TYPE", StringLogical.INSTANCE, k-> dbnull()),
            mapper("IS_AUTOINCREMENT", StringLogical.INSTANCE, k-> stringOf("NO")),
            mapper("IS_GENERATEDCOLUMN", StringLogical.INSTANCE, k-> stringOf("NO"))
            );

    public static ResultSet asResultSet(MillConnection connection, String catalog, String schemaPattern, String tableNamePattern, String columnNamePattern) throws SQLException {
        return new ColumnsMetadata(connection).asResultSet(catalog, schemaPattern, tableNamePattern, columnNamePattern);
    }

    private ResultSet asResultSet(String catalog, String schemaPattern, String tableNamePattern, String columnNamePattern) throws SQLException {
        val allColumns = getAllColumns();
        return ObjectToVectorProducer.resultSet(MAPPINGS, allColumns);
    }

    private List<ColumnsMetadata.ColumnRecord> getAllColumns() {
        log.log(Level.INFO, "Getting all tables");
        val client = this.connection.createClient();
        val stub = client.newBlockingStub();

        val allSchemas = stub
                .listSchemas(ListSchemasRequest.newBuilder().build())
                .getSchemasList();
        val allColumns = new ArrayList<ColumnRecord>();
        for (val schemaName : allSchemas) {
            val schema = stub
                    .getSchema(GetSchemaRequest.newBuilder()
                            .setSchemaName(schemaName)
                            .build())
                    .getSchema();
            for (val table : schema.getTablesList()) {
                val tableColumns = getColumns(schemaName, table);
                allColumns.addAll(tableColumns);
            }
        }
        return allColumns;
    }

    private List<ColumnRecord> getColumns(String schemaName, Table table) {
        val columns = new ArrayList<ColumnRecord>();
        for (val field: table.getFieldsList()) {
            val fieldType = field.getType();
            columns.add(new ColumnRecord(null, schemaName, table.getName(), field.getName(), field.getFieldIdx()+1,
                    sqlDataType(fieldType), sqlDataTypeName(fieldType), sizeOf(fieldType), decimalDigits(fieldType), numPrecRadix(fieldType),
                    nullable(fieldType), nullableLabel(fieldType), sizeBytes(fieldType)));
        }
        return columns;
    }

    private int sizeBytes(DataType fieldType) {
        //physical layout unknown
        return -1;
    }

    private Integer sizeOf(DataType fieldType) {
        return fieldType.getType().getPrecision();
    }

    private Integer decimalDigits(DataType fieldType) {
        return fieldType.getType().getScale();
    }

    private Integer numPrecRadix(DataType fieldType) {
        //assuming JVM types used base 2 used to express scale and precision
        return 2;
    }


    private int nullable(DataType fieldType) {
        return JdbcUtils.jdbcNullability(fieldType.getNullability());
    }

    private String nullableLabel(DataType fieldType) {
        return JdbcUtils.jdbcNullabilityLabel(fieldType.getNullability());
    }


    private Integer sqlDataType(DataType fieldType) {
        return JdbcUtils.logicalTypeIdToJdbcTypeId(fieldType.getType().getTypeId());
    }

    private String sqlDataTypeName(DataType fieldType) {
        return JdbcUtils.logicalTypeIdToJdbcTypeName(fieldType.getType().getTypeId());
    }
}
