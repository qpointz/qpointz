package io.qpointz.mill.test.scenario;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.val;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Root descriptor for a test scenario configuration.
 */
public record Scenario(
        /**
         * The name of the scenario.
         */
        @JsonProperty("name") String name,

        /**
         * Optional parameters for the scenario.
         * These can be used to configure the scenario execution context.
         */
        @JsonProperty("parameters") Map<String, Object> parameters,

        /**
         * List of actions to execute in the scenario.
         * Actions can be executed in any order (flattened structure).
         */
        @JsonProperty("run") List<Action> run
) {
    public Scenario {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Scenario name cannot be null or blank");
        }
        if (parameters == null) {
            parameters = Map.of();
        }
        if (run == null) {
            run = List.of();
        }
    }

    /**
     * Convenience constructor without parameters.
     * 
     * @param name the scenario name
     * @param run the list of actions
     */
    public Scenario(String name, List<Action> run) {
        this(name, Map.of(), run);
    }

    /**
     * Load a Scenario from an InputStream containing YAML.
     *
     * @param inputStream the input stream containing YAML
     * @return the deserialized Scenario
     * @throws IOException if reading or parsing fails
     */
    public static Scenario from(InputStream inputStream) throws IOException {
        val mapper = new ObjectMapper(new YAMLFactory());
        mapper.findAndRegisterModules();
        return mapper.readValue(inputStream, Scenario.class);
    }

    /**
     * Load a Scenario from a file path.
     *
     * @param filePath the path to the YAML file
     * @return the deserialized Scenario
     * @throws IOException if reading or parsing fails
     */
    public static Scenario fromFile(String filePath) throws IOException {
        try (val inputStream = Scenario.class.getClassLoader().getResourceAsStream(filePath)) {
            if (inputStream == null) {
                throw new IOException("Resource not found: " + filePath);
            }
            return from(inputStream);
        }
    }

    /**
     * Load a Scenario from a file path (using java.nio.file.Path).
     *
     * @param filePath the path to the YAML file
     * @return the deserialized Scenario
     * @throws IOException if reading or parsing fails
     */
    public static Scenario fromFile(java.nio.file.Path filePath) throws IOException {
        try (val inputStream = java.nio.file.Files.newInputStream(filePath)) {
            return from(inputStream);
        }
    }

    /**
     * Gets a parameter value by key and converts it to the specified type.
     * Uses Jackson's ObjectMapper to perform the conversion.
     * If the parameter is not present, returns empty Optional.
     * If the parameter is already of the requested type, returns it directly.
     * Otherwise, attempts to convert using Jackson.
     *
     * @param <T> the target type
     * @param key the parameter key
     * @param type the target class
     * @return Optional containing the converted parameter value, or empty if parameter is not present
     * @throws IllegalArgumentException if conversion fails
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<T> paramAs(String key, Class<T> type) {
        if (!parameters.containsKey(key)) {
            return Optional.empty();
        }

        Object value = parameters.get(key);

        // If already of the requested type, return directly
        if (type.isInstance(value)) {
            return Optional.of((T) value);
        }

        // Use Jackson to convert
        try {
            ObjectMapper mapper = new ObjectMapper();
            T converted = mapper.convertValue(value, type);
            return Optional.of(converted);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Failed to convert parameter '" + key + "' to " + type.getSimpleName() + ": " + e.getMessage(), e);
        }
    }
}

