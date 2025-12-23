package io.qpointz.mill.sql.dialect;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.qpointz.mill.MillRuntimeException;
import io.qpointz.mill.utils.YamlUtils;
import lombok.val;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Complete SQL dialect specification that can be deserialized from YAML files.
 * 
 * This record mirrors the structure of SQL dialect YAML files found in 
 * ai/mill-ai-core/src/main/resources/templates/nlsql/dialects/{dialect}/{dialect}.yml
 * 
 * @param id Dialect identifier (e.g., "POSTGRES", "MYSQL", "CALCITE")
 * @param name Dialect display name (e.g., "PostgreSQL", "MySQL", "Apache Calcite")
 * @param notes Optional notes about the dialect
 * @param identifiers Identifier handling configuration
 * @param literals Literal syntax configuration
 * @param joins Join syntax configuration
 * @param ordering ORDER BY clause configuration
 * @param paging Result set paging configuration
 * @param operators Map of operator category names to lists of operators (e.g., "equality", "inequality", "comparison")
 * @param functions Map of function category names to lists of functions (e.g., "strings", "aggregates", "dates_times")
 */
public record SqlDialectSpec(
    @JsonProperty("id") String id,
    @JsonProperty("name") String name,
    @JsonProperty("notes") Optional<List<String>> notes,
    @JsonProperty("identifiers") Identifiers identifiers,
    @JsonProperty("literals") Literals literals,
    @JsonProperty("joins") Joins joins,
    @JsonProperty("ordering") Ordering ordering,
    @JsonProperty("paging") Paging paging,
    @JsonProperty("operators") Map<String, List<OperatorEntry>> operators,
    @JsonProperty("functions") Map<String, List<FunctionEntry>> functions
) {
    /**
     * Deserialize a SqlDialectSpec from a resource stream.
     * 
     * @param inputStream Input stream containing YAML content
     * @return Deserialized SqlDialectSpec
     * @throws MillRuntimeException if deserialization fails
     */
    public static SqlDialectSpec fromInputStream(InputStream inputStream) {
        try {
            val mapper = YamlUtils.defaultYamlMapper();
            return mapper.readValue(inputStream, SqlDialectSpec.class);
        } catch (IOException e) {
            throw new MillRuntimeException("Failed to deserialize SQL dialect specification", e);
        }
    }

    /**
     * Deserialize a SqlDialectSpec from a classpath resource.
     * 
     * @param resourceLocation Classpath resource location (e.g., "templates/nlsql/dialects/postgres/postgres.yml")
     * @return Deserialized SqlDialectSpec
     * @throws MillRuntimeException if resource is not found or deserialization fails
     */
    public static SqlDialectSpec fromResource(String resourceLocation) {
        try (val in = SqlDialectSpec.class.getClassLoader().getResourceAsStream(resourceLocation)) {
            if (in == null) {
                throw new MillRuntimeException("Resource not found: " + resourceLocation);
            }
            return fromInputStream(in);
        } catch (IOException e) {
            throw new MillRuntimeException("Failed to load SQL dialect specification from resource: " + resourceLocation, e);
        }
    }
}
