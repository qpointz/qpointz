package io.qpointz.mill.metadata.database;

import io.qpointz.mill.MillCodeException;
import io.qpointz.mill.MillConnection;
import io.qpointz.mill.metadata.ResultSetProvidingMetadata;
import io.qpointz.mill.proto.GetSchemaRequest;
import io.qpointz.mill.proto.ListSchemasRequest;
import io.qpointz.mill.types.logical.StringLogical;
import io.qpointz.mill.vectors.ObjectToVectorProducer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.java.Log;
import lombok.val;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Stream;

import static io.qpointz.mill.vectors.ObjectToVectorProducer.mapper;
import static io.qpointz.mill.metadata.database.MetadataUtils.*;

@Log
@AllArgsConstructor
/**
 * TablesMetadata is a specialized class that extends ResultSetProvidingMetadata
 * to provide metadata information about tables in a connected database. It filters
 * and retrieves metadata such as catalog, schema, table name, table type, and comments
 * based on provided patterns and table types.
 */
public class TablesMetadata extends ResultSetProvidingMetadata<TablesMetadata.TableRecord> {

    @Getter
    private final MillConnection connection;

    @Getter
    private final String catalogPattern;

    @Getter
    private final String schemaPattern;

    @Getter
    private final String tableNamePattern;

    @Getter
    private final String[] typesPattern;

    /**
     * Returns the list of mappers for mapping TableRecord to logical data types.
     * @return List of MapperInfo for TableRecord
     */
    @Override
    protected List<ObjectToVectorProducer.MapperInfo<TableRecord, ?>> getMappers() {
        return  MAPPINGS;
    }

    /**
     * Represents a record of table metadata.
     * @param catalog - Catalog name
     * @param schema - Schema name
     * @param name - Table name
     * @param type - Table type
     * @param comment - Comments on the table
     */
    protected record TableRecord(String catalog, String schema, String name, String type, String comment) {}

    private static final List<ObjectToVectorProducer.MapperInfo<TableRecord,?>> MAPPINGS = List.of(
            mapper("TABLE_CAT", StringLogical.INSTANCE, k-> dbnull()),
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

    /**
     * Retrieves and filters the table metadata based on the provided patterns and types.
     * @return Filtered collection of TableRecord
     */
    @Override
    protected Collection<TableRecord> getMetadata() {
        Collection<TableRecord> allTables = null;
        try {
            allTables = getAllTables();
        } catch (MillCodeException e) {
            throw new RuntimeException(e);
        }
        log.info(String.format("Filter tables by cat:%s, schema:%s, table:%s, types:%s", this.catalogPattern, this.schemaPattern, this.tableNamePattern, this.typesPattern));
        allTables = this.filterByPattern(this.catalogPattern, allTables, k-> k.catalog);
        allTables = this.filterByPattern(this.schemaPattern, allTables, k-> k.schema);
        allTables = this.filterByPattern(this.tableNamePattern, allTables, k -> k.name);

        Set<String> types = this.typesPattern == null ? null : Set.of(this.typesPattern);
        allTables = this.filterByPredicate(types, allTables, (k,s)-> s.isEmpty() || s.contains(k.type));

        return allTables;
    }

    /**
     * Retrieves all table records from the database based on schemas.
     * @return Collection of all TableRecord
     */
    private Collection<TableRecord> getAllTables() throws MillCodeException {
        log.log(Level.INFO, "Getting all tables");
        val client = this.connection.getClient();

        val allSchemas = client
                .listSchemas(ListSchemasRequest.newBuilder().build())
                .getSchemasList();
        val allTables = new ArrayList<TableRecord>();
        for (val schemaName : allSchemas) {
            val schema = client
                    .getSchema(GetSchemaRequest.newBuilder()
                            .setSchemaName(schemaName)
                            .build())
                    .getSchema();
            for (val table: schema.getTablesList()) {
                var tableType = "TABLE";
                switch (table.getTableType()) {
                    case VIEW:
                        tableType = "VIEW";
                        break;
                    case TABLE, NOT_SPECIFIED_TABLE_TYPE, UNRECOGNIZED:
                        tableType = "TABLE";
                        break;
                }
                val tableRecord = new TableRecord(null, schemaName, table.getName(), tableType, "");
                allTables.add(tableRecord);
            }
        }
        return allTables;
    }

}
