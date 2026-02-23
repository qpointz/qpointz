package io.qpointz.mill.metadata;

import io.qpointz.mill.metadata.impl.file.FileRepository;
import io.qpointz.mill.metadata.model.Model;
import io.qpointz.mill.metadata.model.Relation;
import io.qpointz.mill.metadata.model.Schema;
import io.qpointz.mill.metadata.model.Table;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public interface MetadataProvider {

    Model getModel();
    Collection<Schema> getSchemas();
    Collection<Table> getTables(String schemaName);
    Collection<Relation> getRelations();
    Optional<Table> getTable(String schemaName, String tableName);
    
    /**
     * Get all static value mappings from metadata for RAG ingestion.
     * Returns a flat list of all value mappings including expanded aliases.
     * 
     * @return collection of value mappings with their context (schema, table, attribute)
     */
    Collection<ValueMappingWithContext> getAllValueMappings();
    
    /**
     * Get all SQL-based value mapping sources from metadata for dynamic RAG ingestion.
     * Returns a flat list of all enabled SQL sources.
     * 
     * @return collection of value mapping sources with their context
     */
    Collection<ValueMappingSourceWithContext> getAllValueMappingSources();
    
    /**
     * Value mapping with context information for RAG ingestion.
     * Represents a single user-term to database-value mapping.
     */
    record ValueMappingWithContext(
        String schemaName,
        String tableName,
        String attributeName,
        Optional<String> attributeContext,
        Optional<Double> similarityThreshold,
        FileRepository.ValueMapping mapping
    ) {
        /**
         * Get fully qualified attribute name: "SCHEMA.TABLE.ATTRIBUTE"
         */
        public String getFullyQualifiedName() {
            return String.format("%s.%s.%s", schemaName, tableName, attributeName);
        }
        
        /**
         * Create embedding text for vector search.
         * Combines user term, aliases, attribute name, table name, and description.
         * 
         * @return text optimized for RAG semantic search
         */
        public String toEmbeddingText() {
            var terms = new StringBuilder();
            
            // Context prefix if provided
            attributeContext.filter(ctx -> !ctx.isBlank()).ifPresent(ctx -> {
                terms.append(ctx).append(": ").append(mapping.userTerm()).append(" ");
            });
            
            // Primary user term
            terms.append(mapping.userTerm()).append(" ");
            
            // Aliases
            mapping.aliases().ifPresent(aliases -> {
                aliases.forEach(alias -> terms.append(alias).append(" "));
            });
            
            // Context for better matching
            terms.append(attributeName).append(" ");
            terms.append(tableName).append(" ");
            attributeContext.filter(ctx -> !ctx.isBlank()).ifPresent(ctx -> terms.append(ctx).append(" "));
            
            // Description if available
            mapping.description().ifPresent(desc -> terms.append(desc).append(" "));
            
            return terms.toString().trim();
        }
        
        /**
         * Create a formatted document string for display.
         * 
         * @return human-readable document text
         */
        public String toDocument() {
            var doc = new StringBuilder();
            doc.append("Attribute: ").append(getFullyQualifiedName()).append("\n");
            doc.append("User Term: ").append(mapping.userTerm()).append("\n");
            doc.append("Database Value: ").append(mapping.databaseValue()).append("\n");
            doc.append("Display Value: ").append(mapping.getDisplayValueOrDefault()).append("\n");
            doc.append("Language: ").append(mapping.getLanguageCode()).append("\n");
            
            mapping.description().ifPresent(desc -> 
                doc.append("Description: ").append(desc).append("\n")
            );
            
            mapping.aliases().ifPresent(aliases -> {
                if (!aliases.isEmpty()) {
                    doc.append("Aliases: ").append(String.join(", ", aliases)).append("\n");
                }
            });
            
            return doc.toString();
        }
        
        /**
         * Expand this mapping into multiple mappings, one for each alias.
         * Includes the original mapping plus one for each alias.
         * 
         * @return stream of mappings (original + aliases)
         */
        public Stream<ValueMappingWithContext> expand() {
            Stream<ValueMappingWithContext> original = Stream.of(this);
            
            // Create a mapping for each alias
            Stream<ValueMappingWithContext> fromAliases = mapping.aliases()
                .orElse(List.of())
                .stream()
                .map(alias -> {
                    var aliasMapping = new FileRepository.ValueMapping(
                        alias,  // alias becomes the user-term
                        mapping.databaseValue(),
                        mapping.displayValue(),
                        Optional.of("Alias of " + mapping.userTerm()),  // description
                        mapping.language(),
                        Optional.empty()  // no nested aliases
                    );
                    return new ValueMappingWithContext(
                        schemaName,
                        tableName,
                        attributeName,
                        attributeContext,
                        similarityThreshold,
                        aliasMapping
                    );
                });
            
            return Stream.concat(original, fromAliases);
        }
    }
    
    /**
     * SQL-based value mapping source with context information.
     * Represents a dynamic SQL query that generates value mappings.
     */
    record ValueMappingSourceWithContext(
        String schemaName,
        String tableName,
        String attributeName,
        Optional<String> attributeContext,
        Optional<Double> similarityThreshold,
        String sourceName,
        String sql,
        String description,
        boolean enabled,
        int cacheTtlSeconds
    ) {
        /**
         * Get fully qualified attribute name: "SCHEMA.TABLE.ATTRIBUTE"
         */
        public String getFullyQualifiedName() {
            return String.format("%s.%s.%s", schemaName, tableName, attributeName);
        }
        
        /**
         * Get a unique identifier for this source
         */
        public String getSourceId() {
            return String.format("%s-%s-%s-%s", 
                schemaName, tableName, attributeName, sourceName)
                .replace(".", "-");
        }
    }

}
