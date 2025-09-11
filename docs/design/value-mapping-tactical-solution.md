# Value Mapping - Tactical Solution

**Status:** Implementation Ready  
**Date:** November 5, 2025  
**Purpose:** Quick RAG configuration support while designing full metadata service

---

## Overview

This is a **tactical solution** to enable value mapping configuration in the same YAML format as schema metadata, reusing the existing `FileRepository` structure. This bridges the gap until the full faceted metadata service is implemented.

### Design Goals

1. ✅ **Reuse existing code** - Extend `FileRepository`, minimal new code
2. ✅ **Same file format** - Keep schema and value mappings together
3. ✅ **RAG-ready** - Support for reference data and SQL-based mappings
4. ✅ **Migration path** - Easy to migrate to full metadata service later
5. ✅ **Backward compatible** - Doesn't break existing schema metadata

---

## Extended File Format

### Complete YAML Structure

```yaml
# metadata/complete.yml
model:
  name: production
  description: Production data catalog

schemas:
  - name: MONETA
    description: Banking system schema
    
    tables:
      - name: CUSTOMERS
        description: Customer master data
        
        attributes:
          - name: CUSTOMER_ID
            description: Unique customer identifier
            type: INTEGER
          
          - name: SEGMENT
            description: Customer segmentation
            type: VARCHAR
            
            # ⭐ NEW: Value mappings for this attribute
            value-mappings:
              # Manual mappings
              mappings:
                - user-term: premium
                  database-value: PREMIUM
                  display-value: Premium
                  description: High-value customers
                  language: en
                  
                - user-term: gold
                  database-value: PREMIUM
                  display-value: Premium (Gold)
                  description: Alias for premium segment
                  language: en
                  
                - user-term: básico
                  database-value: BASIC
                  display-value: Básico
                  description: Spanish translation
                  language: es
              
              # ⭐ NEW: Dynamic mappings via SQL
              sources:
                - type: sql
                  name: segment_distinct_values
                  sql: "SELECT DISTINCT segment FROM customers WHERE segment IS NOT NULL"
                  description: Auto-discover segment values
                  enabled: true
                  
                - type: reference-table
                  name: segment_lookup
                  sql: |
                    SELECT 
                      code as database_value,
                      display_name as display_value,
                      description
                    FROM reference.customer_segments
                  description: Reference table for segments
                  enabled: true
          
          - name: COUNTRY_CODE
            description: ISO country code
            type: VARCHAR
            
            value-mappings:
              mappings:
                - user-term: United States
                  database-value: US
                  display-value: United States
                  language: en
                  
                - user-term: USA
                  database-value: US
                  display-value: United States
                  language: en
                  
                - user-term: Canada
                  database-value: CA
                  display-value: Canada
                  language: en
              
              sources:
                - type: reference-table
                  name: country_lookup
                  sql: |
                    SELECT 
                      iso_code_2 as database_value,
                      country_name as display_value,
                      iso_code_2 || ' - ' || country_name as description
                    FROM reference.countries
                    ORDER BY country_name
                  description: ISO 3166-1 alpha-2 country codes
                  enabled: true
```

---

## Extended Java Records

### Updated FileRepository.java

```java
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
            
            // ⭐ NEW: Value mappings
            @JsonProperty("value-mappings") Optional<ValueMappings> valueMappings
    ) {}
    
    // ⭐ NEW: Value mapping records
    
    /**
     * Value mappings for an attribute
     */
    public record ValueMappings(
            @JsonProperty("mappings") List<ValueMapping> mappings,
            @JsonProperty("sources") List<ValueMappingSource> sources
    ) {
        public ValueMappings {
            // Ensure non-null lists
            if (mappings == null) {
                mappings = List.of();
            }
            if (sources == null) {
                sources = List.of();
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
            // Default language to "en"
            if (language == null || language.isEmpty()) {
                language = Optional.of("en");
            }
        }
        
        /**
         * Get display value, defaulting to database value
         */
        public String getDisplayValueOrDefault() {
            return displayValue.orElse(databaseValue);
        }
        
        /**
         * Get language code
         */
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
            // Default enabled to true
            if (enabled == null || enabled.isEmpty()) {
                enabled = Optional.of(true);
            }
            // Default cache TTL to 1 hour
            if (cacheTtlSeconds == null || cacheTtlSeconds.isEmpty()) {
                cacheTtlSeconds = Optional.of(3600);
            }
        }
        
        /**
         * Check if source is enabled
         */
        public boolean isEnabled() {
            return enabled.orElse(true);
        }
        
        /**
         * Get cache TTL in seconds
         */
        public int getCacheTtl() {
            return cacheTtlSeconds.orElse(3600);
        }
    }
}
```

---

## Value Repository Implementation

### FileBasedValueRepository.java

```java
package io.qpointz.mill.ai.nlsql.components;

import io.qpointz.mill.ai.nlsql.ValueRepository;
import io.qpointz.mill.services.metadata.impl.file.FileRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * File-based value repository using FileRepository YAML format
 */
@Slf4j
public class FileBasedValueRepository implements ValueRepository {
    
    private final FileRepository fileRepository;
    private final Map<String, List<ValueMapping>> cache = new ConcurrentHashMap<>();
    
    public FileBasedValueRepository(Resource resource) throws IOException {
        this.fileRepository = FileRepository.from(resource);
        buildCache();
    }
    
    public FileBasedValueRepository(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
        buildCache();
    }
    
    /**
     * Build in-memory cache of value mappings for fast lookup
     */
    private void buildCache() {
        log.info("Building value mapping cache from file repository");
        
        for (var schema : fileRepository.schemas()) {
            for (var table : schema.tables()) {
                for (var attribute : table.attributes()) {
                    
                    if (attribute.valueMappings().isEmpty()) {
                        continue;
                    }
                    
                    var valueMappings = attribute.valueMappings().get();
                    String key = buildKey(schema.name(), table.name(), attribute.name());
                    
                    List<ValueMapping> mappings = new ArrayList<>();
                    
                    // Add manual mappings
                    for (var mapping : valueMappings.mappings()) {
                        mappings.add(new ValueMapping(
                            mapping.userTerm(),
                            mapping.databaseValue(),
                            mapping.getDisplayValueOrDefault(),
                            mapping.getLanguageCode(),
                            mapping.description().orElse(null)
                        ));
                        
                        // Add aliases
                        if (mapping.aliases().isPresent()) {
                            for (var alias : mapping.aliases().get()) {
                                mappings.add(new ValueMapping(
                                    alias,
                                    mapping.databaseValue(),
                                    mapping.getDisplayValueOrDefault(),
                                    mapping.getLanguageCode(),
                                    mapping.description().orElse(null)
                                ));
                            }
                        }
                    }
                    
                    cache.put(key, mappings);
                    log.debug("Cached {} value mappings for {}", mappings.size(), key);
                }
            }
        }
        
        log.info("Value mapping cache built with {} entries", cache.size());
    }
    
    @Override
    public Optional<String> lookupValue(List<String> targetId, String userTerm) {
        // targetId is [schema, table, attribute]
        if (targetId.size() != 3) {
            log.warn("Invalid targetId size: {}", targetId.size());
            return Optional.empty();
        }
        
        String key = buildKey(targetId.get(0), targetId.get(1), targetId.get(2));
        List<ValueMapping> mappings = cache.get(key);
        
        if (mappings == null || mappings.isEmpty()) {
            log.debug("No mappings found for {}", key);
            return Optional.empty();
        }
        
        // Case-insensitive lookup
        return mappings.stream()
            .filter(m -> m.userTerm().equalsIgnoreCase(userTerm))
            .map(ValueMapping::databaseValue)
            .findFirst();
    }
    
    /**
     * Get all mappings for a specific attribute
     */
    public List<ValueMapping> getMappings(String schema, String table, String attribute) {
        String key = buildKey(schema, table, attribute);
        return cache.getOrDefault(key, List.of());
    }
    
    /**
     * Get all value mapping sources for an attribute
     */
    public List<FileRepository.ValueMappingSource> getSources(String schema, String table, String attribute) {
        return fileRepository.schemas().stream()
            .filter(s -> s.name().equalsIgnoreCase(schema))
            .flatMap(s -> s.tables().stream())
            .filter(t -> t.name().equalsIgnoreCase(table))
            .flatMap(t -> t.attributes().stream())
            .filter(a -> a.name().equalsIgnoreCase(attribute))
            .flatMap(a -> a.valueMappings().stream())
            .flatMap(vm -> vm.sources().stream())
            .filter(FileRepository.ValueMappingSource::isEnabled)
            .toList();
    }
    
    private String buildKey(String schema, String table, String attribute) {
        return String.format("%s.%s.%s", 
            schema.toUpperCase(), 
            table.toUpperCase(), 
            attribute.toUpperCase()
        );
    }
    
    /**
     * Internal value mapping record
     */
    public record ValueMapping(
        String userTerm,
        String databaseValue,
        String displayValue,
        String language,
        String description
    ) {}
}
```

---

## Configuration

### Update ValueMappingConfiguration.java

```java
package io.qpointz.mill.ai.nlsql.configuration;

import io.qpointz.mill.ai.nlsql.ValueMapper;
import io.qpointz.mill.ai.nlsql.ValueRepository;
import io.qpointz.mill.ai.nlsql.components.DefaultValueMapper;
import io.qpointz.mill.ai.nlsql.components.FileBasedValueRepository;
import io.qpointz.mill.ai.nlsql.components.VectorStoreValueMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

@Slf4j
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "mill.ai.value-mapping")
public class ValueMappingConfiguration {
    
    @Getter
    @Setter
    private boolean enabled = false;
    
    @Getter
    @Setter
    private String source; // "file", "vector-store"
    
    @Getter
    @Setter
    private String file; // File path for file-based source
    
    /**
     * Create ValueRepository based on configuration
     */
    @Bean
    @ConditionalOnProperty(prefix = "mill.ai.value-mapping", name = "enabled", havingValue = "true")
    public ValueRepository valueRepository(ResourceLoader resourceLoader) throws Exception {
        log.info("Creating value repository with source: {}", source);
        
        if ("file".equals(source)) {
            if (file == null || file.isBlank()) {
                throw new IllegalArgumentException("mill.ai.value-mapping.file must be set when source=file");
            }
            
            Resource resource = resourceLoader.getResource(file);
            log.info("Loading value mappings from: {}", resource.getURI());
            
            return new FileBasedValueRepository(resource);
        }
        
        // Default or vector-store
        log.warn("Unknown value mapping source: {}, using default (no-op)", source);
        return new NoOpValueRepository();
    }
    
    /**
     * Create ValueMapper based on configuration
     */
    @Bean
    public ValueMapper valueMapper(ValueRepository valueRepository) {
        if (enabled && valueRepository != null && !(valueRepository instanceof NoOpValueRepository)) {
            log.info("Creating VectorStoreValueMapper with file-based repository");
            return new VectorStoreValueMapper(valueRepository);
        }
        
        log.info("Creating DefaultValueMapper (pass-through)");
        return new DefaultValueMapper();
    }
    
    /**
     * No-op repository when value mapping is disabled
     */
    private static class NoOpValueRepository implements ValueRepository {
        @Override
        public Optional<String> lookupValue(java.util.List<String> targetId, String userTerm) {
            return Optional.empty();
        }
    }
}
```

---

## Application Configuration

### application.yml

```yaml
mill:
  # Metadata file location
  metadata:
    file:
      repository:
        path: classpath:metadata/complete.yml
  
  # Value mapping configuration
  ai:
    value-mapping:
      enabled: true
      source: file  # "file" or "vector-store"
      file: classpath:metadata/complete.yml  # Same file as metadata
```

---

## Usage Example

### Example metadata/complete.yml

```yaml
model:
  name: moneta-banking
  description: Moneta Banking System

schemas:
  - name: MONETA
    description: Core banking schema
    
    tables:
      - name: CLIENTS
        description: Client information
        
        attributes:
          - name: CLIENT_ID
            description: Unique client identifier
            type: INTEGER
          
          - name: SEGMENT
            description: Client segment
            type: VARCHAR
            value-mappings:
              mappings:
                - user-term: premium
                  database-value: PREMIUM
                  display-value: Premium
                  language: en
                  
                - user-term: gold
                  database-value: PREMIUM
                  display-value: Premium
                  language: en
                  aliases: [oro, golden]
                  
                - user-term: standard
                  database-value: STANDARD
                  display-value: Standard
                  language: en
                  
                - user-term: estándar
                  database-value: STANDARD
                  display-value: Estándar
                  language: es
                  
                - user-term: basic
                  database-value: BASIC
                  display-value: Basic
                  language: en
                  
                - user-term: básico
                  database-value: BASIC
                  display-value: Básico
                  language: es
              
              sources:
                - type: sql
                  name: segment_values
                  sql: SELECT DISTINCT segment FROM moneta.clients WHERE segment IS NOT NULL
                  description: Discover all segment values
                  enabled: true
          
          - name: COUNTRY_CODE
            description: ISO 3166-1 alpha-2 country code
            type: VARCHAR
            value-mappings:
              mappings:
                - user-term: United States
                  database-value: US
                  display-value: United States
                  language: en
                  aliases: [USA, America, US]
                  
                - user-term: Canada
                  database-value: CA
                  display-value: Canada
                  language: en
                  
                - user-term: México
                  database-value: MX
                  display-value: México
                  language: es
                  aliases: [Mexico]
              
              sources:
                - type: reference-table
                  name: country_lookup
                  sql: |
                    SELECT 
                      code as database_value,
                      name_en as display_value
                    FROM reference.countries
                  description: ISO country codes
                  enabled: true
```

---

## SQL-Based Value Mapping (Future Enhancement)

### SqlBasedValueRepository.java

```java
package io.qpointz.mill.ai.nlsql.components;

import io.qpointz.mill.ai.nlsql.ValueRepository;
import io.qpointz.mill.services.metadata.impl.file.FileRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Value repository that executes SQL queries to populate mappings
 */
@Slf4j
public class SqlBasedValueRepository implements ValueRepository {
    
    private final FileBasedValueRepository fileBasedRepository;
    private final JdbcTemplate jdbcTemplate;
    private final Map<String, CachedMappings> sqlCache = new ConcurrentHashMap<>();
    
    public SqlBasedValueRepository(
        FileBasedValueRepository fileBasedRepository,
        JdbcTemplate jdbcTemplate
    ) {
        this.fileBasedRepository = fileBasedRepository;
        this.jdbcTemplate = jdbcTemplate;
    }
    
    @Override
    public Optional<String> lookupValue(List<String> targetId, String userTerm) {
        // First check file-based mappings (manual)
        Optional<String> fileResult = fileBasedRepository.lookupValue(targetId, userTerm);
        if (fileResult.isPresent()) {
            return fileResult;
        }
        
        // Then check SQL-based mappings
        String key = String.join(".", targetId);
        CachedMappings cached = sqlCache.get(key);
        
        // Refresh cache if expired
        if (cached == null || cached.isExpired()) {
            refreshSqlMappings(targetId);
            cached = sqlCache.get(key);
        }
        
        if (cached != null) {
            return cached.lookup(userTerm);
        }
        
        return Optional.empty();
    }
    
    private void refreshSqlMappings(List<String> targetId) {
        if (targetId.size() != 3) return;
        
        String schema = targetId.get(0);
        String table = targetId.get(1);
        String attribute = targetId.get(2);
        
        // Get SQL sources for this attribute
        List<FileRepository.ValueMappingSource> sources = 
            fileBasedRepository.getSources(schema, table, attribute);
        
        if (sources.isEmpty()) {
            return;
        }
        
        String key = String.join(".", targetId);
        Map<String, String> mappings = new HashMap<>();
        
        for (var source : sources) {
            if (!source.isEnabled()) {
                continue;
            }
            
            try {
                log.debug("Executing SQL for value mapping: {}", source.name());
                
                jdbcTemplate.query(source.sql(), rs -> {
                    String dbValue = rs.getString("database_value");
                    String displayValue = rs.getString("display_value");
                    
                    // Map display value to database value
                    mappings.put(displayValue.toLowerCase(), dbValue);
                    
                    // Also map db value to itself
                    mappings.put(dbValue.toLowerCase(), dbValue);
                });
                
                log.info("Loaded {} mappings from SQL source: {}", 
                    mappings.size(), source.name());
                
            } catch (Exception e) {
                log.error("Error executing SQL mapping source {}: {}", 
                    source.name(), e.getMessage());
            }
        }
        
        // Cache the results
        int ttl = sources.stream()
            .mapToInt(FileRepository.ValueMappingSource::getCacheTtl)
            .min()
            .orElse(3600);
        
        sqlCache.put(key, new CachedMappings(mappings, ttl));
    }
    
    private static class CachedMappings {
        private final Map<String, String> mappings;
        private final Instant expiresAt;
        
        CachedMappings(Map<String, String> mappings, int ttlSeconds) {
            this.mappings = mappings;
            this.expiresAt = Instant.now().plusSeconds(ttlSeconds);
        }
        
        boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
        
        Optional<String> lookup(String userTerm) {
            return Optional.ofNullable(mappings.get(userTerm.toLowerCase()));
        }
    }
}
```

---

## Migration Path to Full Metadata Service

When ready to implement the full metadata service, migration is straightforward:

1. **FileRepository stays** - becomes one persistence option
2. **Value mappings move** to `ValueMappingFacet`
3. **Configuration changes** - point to new metadata service API
4. **Code changes** - minimal, just inject new `ValueRepository` implementation

### Migration Example

**Before (Tactical):**
```java
@Bean
public ValueRepository valueRepository(ResourceLoader loader) {
    return new FileBasedValueRepository(loader.getResource("..."));
}
```

**After (Strategic):**
```java
@Bean
public ValueRepository valueRepository(MetadataService metadataService) {
    return new MetadataServiceValueRepository(metadataService);
}
```

The YAML format remains compatible - just add `facets:` wrapper when migrating.

---

## Benefits of This Approach

1. ✅ **Quick implementation** - Extend existing code, ~200 lines
2. ✅ **Same file** - Schema + value mappings together
3. ✅ **RAG ready** - SQL sources for dynamic mappings
4. ✅ **Type safe** - Java records with validation
5. ✅ **Cache support** - In-memory with TTL
6. ✅ **Multi-language** - Built-in language support
7. ✅ **Migration ready** - Easy path to full metadata service

---

## Next Steps

1. Update `FileRepository.java` with new records
2. Implement `FileBasedValueRepository`
3. Update `ValueMappingConfiguration`
4. Create example `metadata/complete.yml`
5. Test with NL2SQL queries
6. (Optional) Implement `SqlBasedValueRepository` for dynamic mappings

---

**Implementation Time:** ~2-3 hours  
**Testing Time:** ~1 hour  
**Total:** Half day

This tactical solution gets you RAG-ready quickly while maintaining the path to the full metadata service later!

