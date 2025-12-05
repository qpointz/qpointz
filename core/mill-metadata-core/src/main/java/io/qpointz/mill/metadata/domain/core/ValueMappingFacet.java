package io.qpointz.mill.metadata.domain.core;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.qpointz.mill.metadata.domain.AbstractFacet;
import io.qpointz.mill.metadata.domain.MetadataFacet;
import io.qpointz.mill.metadata.domain.ValidationException;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * Value mapping facet - maps user terms to database values for NL2SQL.
 * Entity-bound facet attached to ATTRIBUTE entities.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ValueMappingFacet extends AbstractFacet {
    
    private String context;
    private Double similarityThreshold;
    private List<ValueMapping> mappings = new ArrayList<>();
    private List<ValueMappingSource> sources = new ArrayList<>();
    
    /**
     * Static value mapping entry.
     */
    public record ValueMapping(
        @JsonProperty("user-term") String userTerm,
        @JsonProperty("database-value") String databaseValue,
        @JsonProperty("display-value") String displayValue,
        @JsonProperty("description") String description,
        @JsonProperty("language") String language,
        @JsonProperty("aliases") List<String> aliases
    ) {
        public ValueMapping {
            if (language == null || language.isEmpty()) {
                language = "en";
            }
            if (aliases == null) {
                aliases = new ArrayList<>();
            }
        }
    }
    
    /**
     * Dynamic value mapping source (SQL query, API, etc.).
     */
    public record ValueMappingSource(
        @JsonProperty("type") String type,
        @JsonProperty("name") String name,
        @JsonProperty("definition") String definition,  // YAML uses "definition", can also be "sql"
        @JsonProperty("description") String description,
        @JsonProperty("enabled") Boolean enabled,
        @JsonProperty("cronExpression") String cronExpression,
        @JsonProperty("cache-ttl-seconds") Integer cacheTtlSeconds
    ) {
        public ValueMappingSource {
            if (enabled == null) {
                enabled = true;
            }
            if (cacheTtlSeconds == null) {
                cacheTtlSeconds = 3600;
            }
        }
        
        /**
         * Get SQL definition, checking both "definition" and "sql" fields.
         */
        public String getSql() {
            return definition != null ? definition : null;
        }
        
        /**
         * Check if source is enabled.
         */
        public boolean isEnabled() {
            return enabled != null && enabled;
        }
        
        /**
         * Get cache TTL in seconds.
         */
        public int getCacheTtl() {
            return cacheTtlSeconds != null ? cacheTtlSeconds : 3600;
        }
    }
    
    @Override
    public String getFacetType() {
        return "value-mapping";
    }
    
    @Override
    public void validate() throws ValidationException {
        if (mappings != null) {
            for (ValueMapping mapping : mappings) {
                if (mapping.userTerm() == null || mapping.userTerm().isEmpty()) {
                    throw new ValidationException("ValueMappingFacet: userTerm is required for mapping");
                }
                if (mapping.databaseValue() == null || mapping.databaseValue().isEmpty()) {
                    throw new ValidationException("ValueMappingFacet: databaseValue is required for mapping: " + mapping.userTerm());
                }
            }
        }
        if (sources != null) {
            for (ValueMappingSource source : sources) {
                if (source.name() == null || source.name().isEmpty()) {
                    throw new ValidationException("ValueMappingFacet: name is required for source");
                }
                if (source.getSql() == null || source.getSql().isEmpty()) {
                    throw new ValidationException("ValueMappingFacet: definition/sql is required for source: " + source.name());
                }
            }
        }
    }
    
    @Override
    public MetadataFacet merge(MetadataFacet other) {
        if (!(other instanceof ValueMappingFacet)) {
            return this;
        }
        ValueMappingFacet otherFacet = (ValueMappingFacet) other;
        
        // Merge: other takes precedence for context and threshold
        if (otherFacet.context != null) {
            this.context = otherFacet.context;
        }
        if (otherFacet.similarityThreshold != null) {
            this.similarityThreshold = otherFacet.similarityThreshold;
        }
        
        // Merge mappings: combine lists, avoid duplicates by userTerm + databaseValue
        if (otherFacet.mappings != null && !otherFacet.mappings.isEmpty()) {
            this.mappings = new ArrayList<>(this.mappings);
            for (ValueMapping otherMapping : otherFacet.mappings) {
                boolean exists = this.mappings.stream()
                    .anyMatch(m -> m.userTerm().equals(otherMapping.userTerm()) &&
                                 m.databaseValue().equals(otherMapping.databaseValue()));
                if (!exists) {
                    this.mappings.add(otherMapping);
                }
            }
        }
        
        // Merge sources: combine lists, avoid duplicates by name
        if (otherFacet.sources != null && !otherFacet.sources.isEmpty()) {
            this.sources = new ArrayList<>(this.sources);
            for (ValueMappingSource otherSource : otherFacet.sources) {
                boolean exists = this.sources.stream()
                    .anyMatch(s -> s.name().equals(otherSource.name()));
                if (!exists) {
                    this.sources.add(otherSource);
                }
            }
        }
        
        return this;
    }
}

