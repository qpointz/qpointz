package io.qpointz.mill.metadata;

import io.qpointz.mill.MillConnection;
import io.qpointz.mill.proto.GetSchemaRequest;
import io.qpointz.mill.proto.ListSchemasRequest;
import io.qpointz.mill.types.logical.StringLogical;
import io.qpointz.mill.vectors.ObjectToVectorProducer;
import lombok.extern.java.Log;
import lombok.val;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Stream;

import static io.qpointz.mill.vectors.ObjectToVectorProducer.mapper;
import static io.qpointz.mill.metadata.MetadataUtils.*;

@Log
public class TablesMetadata {
    private final MillConnection connection;

    public TablesMetadata(MillConnection connection) {
        this.connection = connection;
    }


    private record TableRecord(String catalog, String schema, String name, String type, String comment) {
    }

    private static List<ObjectToVectorProducer.MapperInfo<TableRecord,?>> MAPPINGS = List.of(
            mapper("TABLE_CAT", StringLogical.INSTANCE, k-> stringOf(k.catalog)),
            mapper("TABLE_SCHEM", StringLogical.INSTANCE, k-> stringOf(k.schema)),
            mapper("TABLE_NAME", StringLogical.INSTANCE, k-> stringOf(k.name)),
            mapper("TABLE_TYPE", StringLogical.INSTANCE, k-> stringOf(k.type)),
            mapper("REMARKS", StringLogical.INSTANCE, k-> stringOf(k.comment)),
            mapper("TYPE_CAT", StringLogical.INSTANCE, k-> Optional.empty()),
            mapper("TYPE_SCHEM", StringLogical.INSTANCE, k-> Optional.empty()),
            mapper("TYPE_NAME", StringLogical.INSTANCE, k-> Optional.empty()),
            mapper("SELF_REFERENCING_COL_NAME", StringLogical.INSTANCE, k-> Optional.empty()),
            mapper("REF_GENERATION", StringLogical.INSTANCE, k-> Optional.empty())
    );

    public static ResultSet asResultSet(MillConnection connection, String catalog, String schemaPattern, String tableNamePattern, String[] types) throws SQLException {
        val tm = new TablesMetadata(connection);
        return tm.asResultSet(catalog, schemaPattern, tableNamePattern, types);
    }

    private <T> Stream<T> applyPattern(Stream<T> in, String pattern, Function<T,Boolean> emptyPredicate, Function<T, Boolean> matchPredicate) {
        if (pattern==null)
            return in;
        if (pattern.isEmpty()) {
            return in.filter(k->!emptyPredicate.apply(k));
        }
        return in.filter(k-> matchPredicate.apply(k));
    }

    private <T> Stream<T> applyPatternOnString(Stream<T> in, String pattern, Function<T,String> value) {
        return applyPattern(in, pattern, k-> value.apply(k).isEmpty(), k-> value.apply(k).equals(pattern));
    }

    private ResultSet asResultSet(String catalog, String schemaPattern, String tableNamePattern, String[] types) throws SQLException {
        var allTables = getAllTables().stream();
        allTables = applyPatternOnString(allTables, catalog, k-> k.catalog);
        allTables = applyPatternOnString(allTables, schemaPattern, k-> k.schema);
        allTables = applyPatternOnString(allTables, tableNamePattern, k-> k.name);
        //java --class-path 'mill-jdbc-driver-0.0.1.jar;sqlline-1.12.0-jar-with-dependencies.jar;lib/*' sqlline.SqlLine -d io.qpointz.mill.Driver -u jdbc:mill://localhost:9099 -e 'select * from `airlines`.`cities`;'
        if (types!=null) {
            val typesList = Arrays.asList(types);
            allTables = allTables.filter(typesList::contains);
        }
        return ObjectToVectorProducer.resultSet(TablesMetadata.MAPPINGS, allTables.toList());
    }

    private List<TableRecord> getAllTables() {
        log.log(Level.INFO, "Getting all tables");
        val client = this.connection.createClient();
        val stub = client.newBlockingStub();

        val allSchemas = stub
                .listSchemas(ListSchemasRequest.newBuilder().build())
                .getSchemasList();
        val allTables = new ArrayList<TableRecord>();
        for (val schemaName : allSchemas) {
            val schema = stub
                    .getSchema(GetSchemaRequest.newBuilder()
                            .setSchemaName(schemaName)
                            .build())
                    .getSchema();
            for (val table: schema.getTablesList()) {
                var tableType = "TABLE";
                switch (table.getTableType()) {
                    case VIEW:
                        tableType = "VIEW";
                    case TABLE, NOT_SPECIFIED_TABLE_TYPE, UNRECOGNIZED:
                        tableType = "TABLE";
                }
                val tableRecord = new TableRecord("", schemaName, table.getName(), tableType, "");
                allTables.add(tableRecord);
            }
        }
        return allTables;
    }


}
