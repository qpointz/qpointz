package io.qpointz.mill.ai.testing.scenario;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for the {@link Action} record class.
 * Tests validation, construction, and parameter handling.
 */
class ActionTest {

    /**
     * Verifies that an Action can be created with a valid key and parameters map.
     * The key identifies the action type (e.g., "do-get", "check-has-sql"),
     * and params contains action-specific configuration.
     */
    @Test
    void shouldCreateAction_whenValidKeyAndParams() {
        // Arrange
        String key = "do-get";
        Map<String, Object> params = Map.of("message", -1);

        // Act
        Action action = new Action(key, params);

        // Assert
        assertThat(action.key()).isEqualTo(key);
        assertThat(action.params()).isEqualTo(params);
    }

    /**
     * Verifies that when params is null, the Action constructor automatically
     * uses an empty map instead. This ensures params is never null.
     */
    @Test
    void shouldUseEmptyMap_whenParamsIsNull() {
        // Arrange
        String key = "do-ask";

        // Act
        Action action = new Action(key, null);

        // Assert
        assertThat(action.key()).isEqualTo(key);
        assertThat(action.params()).isEmpty();
    }

    /**
     * Verifies that creating an Action with a null key throws an IllegalArgumentException.
     * The key is required to identify the action type.
     */
    @Test
    void shouldThrowException_whenKeyIsNull() {
        // Act & Assert
        assertThatThrownBy(() -> new Action(null, Map.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("key cannot be null or blank");
    }

    /**
     * Verifies that creating an Action with a blank key (whitespace only) throws an IllegalArgumentException.
     * Blank keys are not valid action identifiers.
     */
    @Test
    void shouldThrowException_whenKeyIsBlank() {
        // Act & Assert
        assertThatThrownBy(() -> new Action("   ", Map.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("key cannot be null or blank");
    }

    /**
     * Verifies that creating an Action with an empty key throws an IllegalArgumentException.
     * Empty strings are not valid action identifiers.
     */
    @Test
    void shouldThrowException_whenKeyIsEmpty() {
        // Act & Assert
        assertThatThrownBy(() -> new Action("", Map.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("key cannot be null or blank");
    }

    /**
     * Verifies that getParamAs can retrieve and convert a parameter to a specific type.
     * When the parameter exists and is already of the requested type, it should return directly.
     */
    @Test
    void shouldGetParamAs_whenParamExistsAndIsCorrectType() {
        // Arrange
        Map<String, Object> params = new HashMap<>();
        params.put("message", -1);
        params.put("timeout", 5000);
        Action action = new Action("do-get", params);

        // Act
        var result = action.getParamAs("message", Integer.class);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(-1);
    }

    /**
     * Verifies that getParamAs returns empty Optional when the parameter key doesn't exist.
     * This provides safe access to optional parameters.
     */
    @Test
    void shouldReturnEmpty_whenParamKeyDoesNotExist() {
        // Arrange
        Action action = new Action("do-get", Map.of("message", -1));

        // Act
        var result = action.getParamAs("nonexistent", String.class);

        // Assert
        assertThat(result).isEmpty();
    }

    /**
     * Verifies that getParamAs can convert a parameter to a different type using Jackson.
     * Tests the conversion path for parameters that need type transformation.
     */
    @Test
    void shouldConvertParam_whenParamNeedsConversion() {
        // Arrange
        Map<String, Object> params = new HashMap<>();
        params.put("timeout", "5000"); // String that should convert to Integer
        Action action = new Action("do-get", params);

        // Act
        var result = action.getParamAs("timeout", Integer.class);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(5000);
    }

    /**
     * Verifies that getParamAs returns the value directly when it's already of the requested type.
     * No conversion is needed in this case, improving performance.
     */
    @Test
    void shouldReturnDirectly_whenParamIsAlreadyOfRequestedType() {
        // Arrange
        String value = "test-string";
        Action action = new Action("do-ask", Map.of("value", value));

        // Act
        var result = action.getParamAs("value", String.class);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(value);
    }

    /**
     * Verifies that getParamAs throws an IllegalArgumentException when conversion fails.
     * This ensures invalid conversions are caught early with a clear error message.
     */
    @Test
    void shouldThrowException_whenConversionFails() {
        // Arrange
        Action action = new Action("do-get", Map.of("timeout", "not-a-number"));

        // Act & Assert
        assertThatThrownBy(() -> action.getParamAs("timeout", Integer.class))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Failed to convert parameter 'timeout'");
    }

    /**
     * Verifies that getParamAs can handle complex nested objects in parameters.
     * Tests conversion of Map parameters to typed objects.
     */
    @Test
    void shouldGetParamAsMap_whenParamIsMap() {
        // Arrange
        Map<String, Object> nestedMap = new HashMap<>();
        nestedMap.put("key", "value");
        nestedMap.put("number", 42);
        Action action = new Action("check-data", Map.of("expect", nestedMap));

        // Act
        var result = action.getParamAs("expect", Map.class);

        // Assert
        assertThat(result).isPresent();
        @SuppressWarnings("unchecked")
        Map<String, Object> map = result.get();
        assertThat(map).containsEntry("key", "value");
        assertThat(map).containsEntry("number", 42);
    }

    /**
     * Verifies that getParamAs(Class) can convert the entire params map to the requested type.
     * When params is already a Map, it should return it directly.
     */
    @Test
    void shouldGetParamAs_whenConvertingEntireParamsMap() {
        // Arrange
        Map<String, Object> params = new HashMap<>();
        params.put("message", -1);
        params.put("timeout", 5000);
        Action action = new Action("do-get", params);

        // Act
        var result = action.getParamAs(Map.class);

        // Assert
        assertThat(result).isPresent();
        @SuppressWarnings("unchecked")
        Map<String, Object> map = result.get();
        assertThat(map).containsEntry("message", -1);
        assertThat(map).containsEntry("timeout", 5000);
    }

    /**
     * Verifies that getParamAs(Class) returns empty Optional when params is empty.
     * This provides safe access when there are no parameters.
     */
    @Test
    void shouldReturnEmpty_whenParamsIsEmpty() {
        // Arrange
        Action action = new Action("do-get", Map.of());

        // Act
        var result = action.getParamAs(Map.class);

        // Assert
        assertThat(result).isEmpty();
    }

    /**
     * Verifies that getParamAs(Class) returns the params map directly when it's already of the requested type.
     * No conversion is needed in this case, improving performance.
     */
    @Test
    void shouldReturnDirectly_whenParamsIsAlreadyOfRequestedType() {
        // Arrange
        Map<String, Object> params = new HashMap<>();
        params.put("key", "value");
        Action action = new Action("do-get", params);

        // Act
        var result = action.getParamAs(Map.class);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get()).isSameAs(params);
    }

    /**
     * Verifies that getParamAs(Class) throws an IllegalArgumentException when conversion fails.
     * This ensures invalid conversions are caught early with a clear error message.
     */
    @Test
    void shouldThrowException_whenEntireParamsConversionFails() {
        // Arrange
        Map<String, Object> params = new HashMap<>();
        params.put("invalid", "data");
        Action action = new Action("do-get", params);

        // Act & Assert
        assertThatThrownBy(() -> action.getParamAs(Integer.class))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Failed to convert params to Integer");
    }
}
