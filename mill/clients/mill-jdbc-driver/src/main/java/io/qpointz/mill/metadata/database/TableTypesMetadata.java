package io.qpointz.mill.metadata.database;

import io.qpointz.mill.MillConnection;
import io.qpointz.mill.metadata.ResultSetProvidingMetadata;
import io.qpointz.mill.types.logical.StringLogical;
import io.qpointz.mill.vectors.ObjectToVectorProducer;
import lombok.val;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import static io.qpointz.mill.metadata.database.MetadataUtils.stringOf;
import static io.qpointz.mill.vectors.ObjectToVectorProducer.mapper;

/**
 * The TableTypesMetadata class provides metadata information about table types in a database.
 * It contains methods to fetch and convert table types into a format suitable for consumption as metadata.
 */
public class TableTypesMetadata extends ResultSetProvidingMetadata<String> {
    
    /**
     * Mappings define the relationship between database columns and logical type representations.
     * In this case, it maps the "TABLE_TYPE" database field to its logical representation as a String.
     */
    private static final List<ObjectToVectorProducer.MapperInfo<String, ?>> MAPPINGS = List.of(
            mapper("TABLE_TYPE", StringLogical.INSTANCE, k -> stringOf(k))
    );

    /**
     * Provides the defined mappings between table type fields and their logical representations.
     *
     * @return a list of MapperInfo objects representing metadata field mappings.
     */
    @Override
    protected List<ObjectToVectorProducer.MapperInfo<String, ?>> getMappers() {
        return MAPPINGS;
    }

    /**
     * Returns a collection of metadata values representing table types in the database.
     *
     * @return a collection of Strings representing the table types.
     */
    @Override
    protected Collection<String> getMetadata() {
        // Internal list of table types
        return getTableTypes();
    }

    /**
     * Provides a predefined collection of table types in the database.
     *
     * @return a list of Strings representing the table types (e.g., "TABLE", "VIEW").
     */
    private static List<String> getTableTypes() {
        return List.of("TABLE", "VIEW");
    }
}
