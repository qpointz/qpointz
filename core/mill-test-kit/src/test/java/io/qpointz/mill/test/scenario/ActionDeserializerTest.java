package io.qpointz.mill.test.scenario;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for the {@link ActionDeserializer} custom Jackson deserializer.
 * Tests YAML deserialization of Action objects with various value types (object, string, list).
 */
class ActionDeserializerTest {

    private final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

    /**
     * Verifies that an Action with object parameters (nested map) is correctly deserialized.
     * Example YAML: "do-get:\n  message: -1"
     */
    @Test
    void shouldDeserializeObjectValue_whenActionHasObjectParams() throws IOException {
        // Arrange
        String yaml = "do-get:\n  message: -1";

        // Act
        Action action = mapper.readValue(yaml, Action.class);

        // Assert
        assertThat(action.key()).isEqualTo("do-get");
        assertThat(action.params()).containsEntry("message", -1);
    }

    /**
     * Verifies that an Action with a simple string value is correctly deserialized.
     * The string value is stored in params with key "value".
     * Example YAML: "do-ask: question"
     */
    @Test
    void shouldDeserializeStringValue_whenActionHasStringValue() throws IOException {
        // Arrange
        String yaml = "do-ask: question";

        // Act
        Action action = mapper.readValue(yaml, Action.class);

        // Assert
        assertThat(action.key()).isEqualTo("do-ask");
        assertThat(action.params()).containsEntry("value", "question");
    }

    /**
     * Verifies that an Action with a list value is correctly deserialized.
     * The list is stored in params with key "items".
     * Example YAML: "check-data:\n  - expect:\n      columns:\n        - A\n        - B"
     */
    @Test
    void shouldDeserializeListValue_whenActionHasListValue() throws IOException {
        // Arrange
        String yaml = "check-data:\n  - expect:\n      columns:\n        - A\n        - B";

        // Act
        Action action = mapper.readValue(yaml, Action.class);

        // Assert
        assertThat(action.key()).isEqualTo("check-data");
        assertThat(action.params()).containsKey("items");
        @SuppressWarnings("unchecked")
        List<Object> items = (List<Object>) action.params().get("items");
        assertThat(items).isNotEmpty();
    }

    /**
     * Verifies that an Action with a complex nested object structure is correctly deserialized.
     * Tests multiple parameters at the same level.
     * Example YAML: "check-has-sql:\n  pass: WARN\n  timeout: 5000"
     */
    @Test
    void shouldDeserializeNestedObject_whenActionHasComplexStructure() throws IOException {
        // Arrange
        String yaml = "check-has-sql:\n  pass: WARN\n  timeout: 5000";

        // Act
        Action action = mapper.readValue(yaml, Action.class);

        // Assert
        assertThat(action.key()).isEqualTo("check-has-sql");
        assertThat(action.params()).containsEntry("pass", "WARN");
        assertThat(action.params()).containsEntry("timeout", 5000);
    }

    /**
     * Verifies that deserialization fails when the YAML is not an object (e.g., a plain string).
     * Actions must be key-value pairs, not primitive values.
     */
    @Test
    void shouldThrowException_whenYamlIsNotAnObject() {
        // Arrange
        String yaml = "not-an-object";

        // Act & Assert
        assertThatThrownBy(() -> mapper.readValue(yaml, Action.class))
                .isInstanceOf(IOException.class);
    }

    /**
     * Verifies that deserialization fails when the YAML has multiple top-level keys.
     * Actions must have exactly one key (the action type).
     */
    @Test
    void shouldThrowException_whenYamlHasMultipleKeys() {
        // Arrange
        String yaml = "key1: value1\nkey2: value2";

        // Act & Assert
        assertThatThrownBy(() -> mapper.readValue(yaml, Action.class))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("exactly one field");
    }

    /**
     * Verifies that an Action with a "name" parameter extracts the name correctly.
     * The "name" field should be extracted from params and stored separately.
     * Example YAML: "do-get:\n  name: Get message\n  message: -1"
     */
    @Test
    void shouldExtractName_whenActionHasNameParam() throws IOException {
        // Arrange
        String yaml = "do-get:\n  name: Get message\n  message: -1";

        // Act
        Action action = mapper.readValue(yaml, Action.class);

        // Assert
        assertThat(action.key()).isEqualTo("do-get");
        assertThat(action.name()).isPresent();
        assertThat(action.name().get()).isEqualTo("Get message");
        assertThat(action.params()).containsEntry("message", -1);
        assertThat(action.params()).doesNotContainKey("name"); // name should be removed from params
    }

    /**
     * Verifies that an Action without a "name" parameter has empty name.
     */
    @Test
    void shouldHaveEmptyName_whenActionHasNoNameParam() throws IOException {
        // Arrange
        String yaml = "do-get:\n  message: -1";

        // Act
        Action action = mapper.readValue(yaml, Action.class);

        // Assert
        assertThat(action.key()).isEqualTo("do-get");
        assertThat(action.name()).isEmpty();
        assertThat(action.params()).containsEntry("message", -1);
    }
}
