package io.qpointz.mill.test.scenario;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.Map;
import java.util.Optional;

/**
 * Descriptor for a test scenario action.
 * All actions (both "do" and "check" types) are represented uniformly.
 */
@JsonDeserialize(using = ActionDeserializer.class)
public record Action(
        /**
         * The action type key (e.g., "do-get", "do-ask", "check-has-sql", "check-data").
         */
        String key,

        /**
         * Optional name for the action, used for display purposes.
         * If present, this name will be used in test display names instead of the key.
         */
        Optional<String> name,

        /**
         * All child properties of the action deserialized as a map.
         * For object values: params contains the nested properties.
         * For string values: params contains {"value": "..."}.
         * For list values: params contains {"items": [...]}.
         * Note: "name" is extracted from params and stored separately.
         */
        Map<String, Object> params
) {
    public Action {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Action key cannot be null or blank");
        }
        if (name == null) {
            name = Optional.empty();
        }
        if (params == null) {
            params = Map.of();
        }
    }
    
    /**
     * Convenience constructor without name parameter.
     * 
     * @param key the action type key
     * @param params the action parameters
     */
    public Action(String key, Map<String, Object> params) {
        this(key, Optional.empty(), params);
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
    public <T> Optional<T> getParamAs(String key, Class<T> type) {
        if (!params.containsKey(key)) {
            return Optional.empty();
        }

        Object value = params.get(key);

        // If already of the requested type, return directly
        if (type.isInstance(value)) {
            return Optional.of((T) value);
        }

        // Use Jackson to convert
        try {
            ObjectMapper mapper = createObjectMapper();
            T converted = mapper.convertValue(value, type);
            return Optional.of(converted);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Failed to convert parameter '" + key + "' to " + type.getSimpleName() + ": " + e.getMessage(), e);
        }
    }

    /**
     * Converts the entire params map to an instance of the specified type.
     * Uses Jackson's ObjectMapper to perform the conversion.
     * If params is empty, returns empty Optional.
     * If params is already of the requested type, returns it directly.
     * Otherwise, attempts to convert the entire map using Jackson.
     *
     * @param <T> the target type
     * @param type the target class
     * @return Optional containing the converted params, or empty if params is empty
     * @throws IllegalArgumentException if conversion fails
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<T> getParamAs(Class<T> type) {
        if (params.isEmpty()) {
            return Optional.empty();
        }

        // If already of the requested type, return directly
        if (type.isInstance(params)) {
            return Optional.of((T) params);
        }

        // Use Jackson to convert the entire map
        try {
            ObjectMapper mapper = createObjectMapper();
            T converted = mapper.convertValue(params, type);
            return Optional.of(converted);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Failed to convert params to " + type.getSimpleName() + ": " + e.getMessage(), e);
        }
    }

    /**
     * Creates an ObjectMapper for type conversions.
     * Uses a basic ObjectMapper which should handle most common conversions.
     *
     * @return an ObjectMapper
     */
    private static ObjectMapper createObjectMapper() {
        return new ObjectMapper();
    }
}


