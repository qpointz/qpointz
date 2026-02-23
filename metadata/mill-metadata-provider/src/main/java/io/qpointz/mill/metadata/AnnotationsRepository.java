package io.qpointz.mill.metadata;

import java.util.Collection;
import java.util.Optional;

public interface AnnotationsRepository {
    Optional<String> getModelName();

    Optional<String> getModelDescription();

    Optional<String> getSchemaDescription(String schemaName);

    Optional<String> getTableDescription(String schemaName, String tableName);

    Optional<String> getAttributeDescription(String schemaName, String tableName, String attributeNam);
    
    /**
     * Get all static value mappings from metadata.
     * Returns a flat list of all mappings from all attributes.
     * 
     * @return collection of value mappings with context
     */
    Collection<MetadataProvider.ValueMappingWithContext> getAllValueMappings();
    
    /**
     * Get all SQL-based value mapping sources from metadata.
     * Returns a flat list of all sources that are enabled.
     * 
     * @return collection of value mapping sources with context
     */
    Collection<MetadataProvider.ValueMappingSourceWithContext> getAllValueMappingSources();
}
