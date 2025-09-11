package io.qpointz.mill.services.metadata.impl.file;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.val;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public record FileRepository(
        @JsonProperty("model") Model model,
        @JsonProperty("schemas") Collection<Schema> schemas
) {
    public static FileRepository from(String location,
                                             ResourceLoader resourceLoader) throws IOException {
        return from(resourceLoader.getResource(location));
    }

    public static FileRepository from(Resource resource) throws IOException {
        return from(resource.getInputStream());
    }

    public static FileRepository from(InputStream inputStream) throws IOException {
        val mapper = new ObjectMapper(new YAMLFactory());
        mapper.findAndRegisterModules();
        val result = mapper.readValue(inputStream, FileRepository.class);
        return result;
    }

    public record Model(
            @JsonProperty("name") Optional<String> name,
            @JsonProperty("description") Optional<String> description
    ) {}

    public record Schema (
            @JsonProperty("name") String name,
            @JsonProperty("description") Optional<String> description,
            @JsonProperty("tables") List<Table> tables,
            @JsonProperty("references") List<Relation> relations
    ) {}

    public record Relation(
            @JsonProperty("description") Optional<String> description,
            @JsonProperty("parent") RelationParty parent,
            @JsonProperty("child") RelationParty child,
            @JsonProperty("cardinality") Optional<String> cardinality
    ) {}

    public record RelationParty(
            @JsonProperty("table") String table,
            @JsonProperty("attribute") String attribute
    ) {}

    public record Table(
            @JsonProperty("name") String name,
            @JsonProperty("description") Optional<String> description,
            @JsonProperty("attributes") List<Attribute> attributes
    ) {}

    public record Attribute(
            @JsonProperty("name") String name,
            @JsonProperty("description") Optional<String> description,
            @JsonProperty("type") Optional<String> typeName,
            @JsonProperty("value-mappings") Optional<ValueMappings> valueMappings
    ) {}
    
    /**
     * Value mappings for an attribute (tactical solution for RAG)
     */
    public record ValueMappings(
            @JsonProperty("mappings") List<ValueMapping> mappings,
            @JsonProperty("sources") List<ValueMappingSource> sources,
            @JsonProperty("context") Optional<String> context,
            @JsonProperty("similarity-threshold") Optional<Double> similarityThreshold
    ) {
        public ValueMappings {
            if (mappings == null) {
                mappings = List.of();
            }
            if (sources == null) {
                sources = List.of();
            }
            if (context == null) {
                context = Optional.empty();
            }
            if (similarityThreshold == null) {
                similarityThreshold = Optional.empty();
            }
        }
    }
    
    /**
     * Single value mapping entry
     */
    public record ValueMapping(
            @JsonProperty("user-term") String userTerm,
            @JsonProperty("database-value") String databaseValue,
            @JsonProperty("display-value") Optional<String> displayValue,
            @JsonProperty("description") Optional<String> description,
            @JsonProperty("language") Optional<String> language,
            @JsonProperty("aliases") Optional<List<String>> aliases
    ) {
        public ValueMapping {
            if (language == null || language.isEmpty()) {
                language = Optional.of("en");
            }
        }
        
        public String getDisplayValueOrDefault() {
            return displayValue.orElse(databaseValue);
        }
        
        public String getLanguageCode() {
            return language.orElse("en");
        }
    }
    
    /**
     * Dynamic value mapping source (SQL query, reference table, etc.)
     */
    public record ValueMappingSource(
            @JsonProperty("type") String type,
            @JsonProperty("name") String name,
            @JsonProperty("sql") String sql,
            @JsonProperty("description") Optional<String> description,
            @JsonProperty("enabled") Optional<Boolean> enabled,
            @JsonProperty("cron") Optional<String> cron,
            @JsonProperty("cache-ttl-seconds") Optional<Integer> cacheTtlSeconds
    ) {
        public ValueMappingSource {
            if (enabled == null || enabled.isEmpty()) {
                enabled = Optional.of(true);
            }
            if (cacheTtlSeconds == null || cacheTtlSeconds.isEmpty()) {
                cacheTtlSeconds = Optional.of(3600);
            }
        }
        
        public boolean isEnabled() {
            return enabled.orElse(true);
        }
        
        public int getCacheTtl() {
            return cacheTtlSeconds.orElse(3600);
        }
    }
}

