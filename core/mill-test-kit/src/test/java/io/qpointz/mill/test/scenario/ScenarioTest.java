package io.qpointz.mill.test.scenario;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for the {@link Scenario} record class.
 * Tests construction, validation, and YAML deserialization from various sources.
 */
class ScenarioTest {

    /**
     * Verifies that a Scenario can be created with a valid name and list of actions.
     * The name identifies the scenario, and run contains the sequence of actions to execute.
     */
    @Test
    void shouldCreateScenario_whenValidNameAndRun() {
        // Arrange
        String name = "test-scenario";
        List<Action> run = List.of(new Action("do-get", Map.of()));

        // Act
        Scenario scenario = new Scenario(name, Map.of(), run);

        // Assert
        assertThat(scenario.name()).isEqualTo(name);
        assertThat(scenario.run()).isEqualTo(run);
    }

    @Test
    void shouldUseEmptyList_whenRunIsNull() {
        // Arrange
        String name = "test-scenario";

        // Act
        Scenario scenario = new Scenario(name, Map.of(), null);

        // Assert
        assertThat(scenario.name()).isEqualTo(name);
        assertThat(scenario.run()).isEmpty();
    }

    /**
     * Verifies that creating a Scenario with a null name throws an IllegalArgumentException.
     * The name is required to identify the scenario.
     */
    @Test
    void shouldThrowException_whenNameIsNull() {
        // Act & Assert
        assertThatThrownBy(() -> new Scenario(null, Map.of(), List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Scenario name cannot be null or blank");
    }

    /**
     * Verifies that creating a Scenario with a blank name (whitespace only) throws an IllegalArgumentException.
     * Blank names are not valid scenario identifiers.
     */
    @Test
    void shouldThrowException_whenNameIsBlank() {
        // Act & Assert
        assertThatThrownBy(() -> new Scenario("   ", Map.of(), List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Scenario name cannot be null or blank");
    }

    /**
     * Verifies that a Scenario can be deserialized from an InputStream containing valid YAML.
     * Tests the from(InputStream) factory method.
     */
    @Test
    void shouldDeserializeFromInputStream_whenValidYaml() throws IOException {
        // Arrange
        String yaml = "name: test-scenario\nrun:\n  - do-get:\n      message: -1";
        InputStream inputStream = new ByteArrayInputStream(yaml.getBytes());

        // Act
        Scenario scenario = Scenario.from(inputStream);

        // Assert
        assertThat(scenario.name()).isEqualTo("test-scenario");
        assertThat(scenario.run()).hasSize(1);
        assertThat(scenario.run().get(0).key()).isEqualTo("do-get");
    }

    /**
     * Verifies that a Scenario can be deserialized from a file path containing valid YAML.
     * Tests the fromFile(Path) factory method with a temporary file.
     */
    @Test
    void shouldDeserializeFromFile_whenValidYamlFile() throws IOException {
        // Arrange
        String yaml = "name: test-scenario\nrun:\n  - do-ask: question";
        Path tempFile = Files.createTempFile("test-scenario", ".yml");
        Files.write(tempFile, yaml.getBytes());

        try {
            // Act
            Scenario scenario = Scenario.fromFile(tempFile);

            // Assert
            assertThat(scenario.name()).isEqualTo("test-scenario");
            assertThat(scenario.run()).hasSize(1);
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    /**
     * Verifies that deserializing from a non-existent file throws an IOException.
     * Tests error handling for missing files.
     */
    @Test
    void shouldThrowException_whenFileNotFound() {
        // Act & Assert
        assertThatThrownBy(() -> Scenario.fromFile("non-existent-file.yml"))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("Resource not found");
    }

    /**
     * Verifies that a complex scenario with multiple actions of different types
     * can be correctly deserialized. Tests the full scenario structure.
     */
    @Test
    void shouldDeserializeComplexScenario_whenYamlHasMultipleActions() throws IOException {
        // Arrange
        String yaml = """
                name: complex-scenario
                run:
                  - do-get:
                      message: -1
                  - do-ask: question
                  - check-has-sql:
                      pass: WARN
                """;
        InputStream inputStream = new ByteArrayInputStream(yaml.getBytes());

        // Act
        Scenario scenario = Scenario.from(inputStream);

        // Assert
        assertThat(scenario.name()).isEqualTo("complex-scenario");
        assertThat(scenario.run()).hasSize(3);
        assertThat(scenario.run().get(0).key()).isEqualTo("do-get");
        assertThat(scenario.run().get(1).key()).isEqualTo("do-ask");
        assertThat(scenario.run().get(2).key()).isEqualTo("check-has-sql");
    }

    /**
     * Verifies that null parameters default to an empty map.
     */
    @Test
    void shouldUseEmptyMap_whenParametersIsNull() {
        // Arrange
        String name = "test-scenario";
        List<Action> run = List.of();

        // Act
        Scenario scenario = new Scenario(name, null, run);

        // Assert
        assertThat(scenario.parameters()).isEmpty();
    }

    /**
     * Verifies that parameters are correctly deserialized from YAML.
     */
    @Test
    void shouldDeserializeParameters_whenYamlHasParameters() throws IOException {
        // Arrange
        String yaml = """
                name: test-scenario
                parameters:
                  runner: chat-app
                  reasoner: default
                run:
                  - do-get:
                      message: -1
                """;
        InputStream inputStream = new ByteArrayInputStream(yaml.getBytes());

        // Act
        Scenario scenario = Scenario.from(inputStream);

        // Assert
        assertThat(scenario.parameters()).containsEntry("runner", "chat-app");
        assertThat(scenario.parameters()).containsEntry("reasoner", "default");
    }

    /**
     * Verifies that parameters with multiple types (strings, numbers, booleans) are correctly deserialized.
     */
    @Test
    void shouldDeserializeParametersWithMultipleTypes() throws IOException {
        // Arrange
        String yaml = """
                name: test-scenario
                parameters:
                  runner: chat-app
                  timeout: 5000
                  enabled: true
                run: []
                """;
        InputStream inputStream = new ByteArrayInputStream(yaml.getBytes());

        // Act
        Scenario scenario = Scenario.from(inputStream);

        // Assert
        assertThat(scenario.parameters()).containsEntry("runner", "chat-app");
        assertThat(scenario.parameters()).containsEntry("timeout", 5000);
        assertThat(scenario.parameters()).containsEntry("enabled", true);
    }

    /**
     * Verifies that a scenario without a parameters section works correctly.
     */
    @Test
    void shouldDeserializeScenarioWithoutParameters() throws IOException {
        // Arrange
        String yaml = """
                name: test-scenario
                run:
                  - do-get:
                      message: -1
                """;
        InputStream inputStream = new ByteArrayInputStream(yaml.getBytes());

        // Act
        Scenario scenario = Scenario.from(inputStream);

        // Assert
        assertThat(scenario.parameters()).isEmpty();
        assertThat(scenario.run()).hasSize(1);
    }

    /**
     * Verifies that paramAs returns empty Optional when parameter key does not exist.
     */
    @Test
    void shouldReturnEmpty_whenParamKeyDoesNotExist() {
        // Arrange
        Scenario scenario = new Scenario("test", Map.of("other", "value"), List.of());

        // Act & Assert
        assertThat(scenario.paramAs("missing", String.class)).isEmpty();
    }

    /**
     * Verifies that paramAs returns the value directly when it's already of the requested type.
     */
    @Test
    void shouldReturnDirectly_whenParamIsAlreadyOfRequestedType() {
        // Arrange
        Scenario scenario = new Scenario("test", Map.of("runner", "chat-app"), List.of());

        // Act
        var result = scenario.paramAs("runner", String.class);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo("chat-app");
    }

    /**
     * Verifies that paramAs converts the parameter when type conversion is needed.
     */
    @Test
    void shouldConvertParam_whenParamNeedsConversion() {
        // Arrange
        Scenario scenario = new Scenario("test", Map.of("number", "123"), List.of());

        // Act
        var result = scenario.paramAs("number", Integer.class);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(123);
    }

    /**
     * Verifies that paramAs throws exception when conversion fails.
     */
    @Test
    void shouldThrowException_whenConversionFails() {
        // Arrange
        Scenario scenario = new Scenario("test", Map.of("invalid", "not-a-number"), List.of());

        // Act & Assert
        assertThatThrownBy(() -> scenario.paramAs("invalid", Integer.class))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Failed to convert parameter");
    }

    /**
     * Verifies that paramAs correctly retrieves string parameters.
     */
    @Test
    void shouldGetParamAsString_whenParamIsString() {
        // Arrange
        Scenario scenario = new Scenario("test", Map.of("runner", "chat-app"), List.of());

        // Act
        var result = scenario.paramAs("runner", String.class);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo("chat-app");
    }

    /**
     * Verifies that paramAs correctly retrieves integer parameters.
     */
    @Test
    void shouldGetParamAsInteger_whenParamIsNumber() {
        // Arrange
        Scenario scenario = new Scenario("test", Map.of("timeout", 5000), List.of());

        // Act
        var result = scenario.paramAs("timeout", Integer.class);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(5000);
    }

    /**
     * Verifies that paramAs correctly retrieves boolean parameters.
     */
    @Test
    void shouldGetParamAsBoolean_whenParamIsBoolean() {
        // Arrange
        Scenario scenario = new Scenario("test", Map.of("enabled", true), List.of());

        // Act
        var result = scenario.paramAs("enabled", Boolean.class);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get()).isTrue();
    }
}
