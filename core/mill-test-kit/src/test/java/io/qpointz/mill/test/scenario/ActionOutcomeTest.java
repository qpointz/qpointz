package io.qpointz.mill.test.scenario;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for the {@link ActionOutcome} record class.
 * Tests factory methods, builder pattern, data conversion, status parsing, and validation.
 */
class ActionOutcomeTest {

    /**
     * Verifies that the simplest factory method creates an outcome with only a status.
     * Data and metrics should be empty Optionals.
     */
    @Test
    void shouldCreateOutcome_whenUsingOfWithStatus() {
        // Act
        ActionOutcome outcome = ActionOutcome.of(ActionOutcome.OutcomeStatus.PASS);

        // Assert
        assertThat(outcome.status()).isEqualTo(ActionOutcome.OutcomeStatus.PASS);
        assertThat(outcome.data()).isEmpty();
        assertThat(outcome.metrics()).isEmpty();
    }

    /**
     * Verifies that an outcome can be created with status and data.
     * The data should be wrapped in an Optional and metrics should be empty.
     */
    @Test
    void shouldCreateOutcome_whenUsingOfWithStatusAndData() {
        // Arrange
        Map<String, Object> data = new HashMap<>();
        data.put("key", "value");

        // Act
        ActionOutcome outcome = ActionOutcome.of(ActionOutcome.OutcomeStatus.PASS, data);

        // Assert
        assertThat(outcome.status()).isEqualTo(ActionOutcome.OutcomeStatus.PASS);
        assertThat(outcome.data()).isPresent();
        assertThat(outcome.data().get()).isEqualTo(data);
        assertThat(outcome.metrics()).isEmpty();
    }

    /**
     * Verifies that an outcome can be created with status and metrics (no data).
     * Uses the ofMetrics factory method to avoid overload ambiguity with Map parameters.
     */
    @Test
    void shouldCreateOutcome_whenUsingOfWithStatusAndMetrics() {
        // Arrange
        Map<String, Object> metrics = Map.of("executionTimeMs", 150);

        // Act
        ActionOutcome outcome = ActionOutcome.ofMetrics(ActionOutcome.OutcomeStatus.PASS, metrics);

        // Assert
        assertThat(outcome.status()).isEqualTo(ActionOutcome.OutcomeStatus.PASS);
        assertThat(outcome.data()).isEmpty();
        assertThat(outcome.metrics()).isPresent();
        assertThat(outcome.metrics().get()).containsEntry("executionTimeMs", 150);
    }

    /**
     * Verifies that an outcome can be created with status, data, and metrics together.
     * All three components should be properly set.
     */
    @Test
    void shouldCreateOutcome_whenUsingOfWithStatusDataAndMetrics() {
        // Arrange
        Map<String, Object> data = Map.of("result", "success");
        Map<String, Object> metrics = Map.of("executionTimeMs", 200);

        // Act
        ActionOutcome outcome = ActionOutcome.of(ActionOutcome.OutcomeStatus.PASS, data, metrics);

        // Assert
        assertThat(outcome.status()).isEqualTo(ActionOutcome.OutcomeStatus.PASS);
        assertThat(outcome.data()).isPresent();
        assertThat(outcome.data().get()).isEqualTo(data);
        assertThat(outcome.metrics()).isPresent();
        assertThat(outcome.metrics().get()).containsEntry("executionTimeMs", 200);
    }

    /**
     * Verifies that the builder pattern can be used to create an outcome with
     * data and multiple metrics. The builder provides a fluent API for complex constructions.
     */
    @Test
    void shouldCreateOutcome_whenUsingBuilder() {
        // Act
        ActionOutcome outcome = ActionOutcome.builder(ActionOutcome.OutcomeStatus.WARN)
                .withData(Map.of("key", "value"))
                .withMetric("executionTimeMs", 150)
                .withMetric("memoryUsed", 1024)
                .build();

        // Assert
        assertThat(outcome.status()).isEqualTo(ActionOutcome.OutcomeStatus.WARN);
        assertThat(outcome.data()).isPresent();
        assertThat(outcome.metrics()).isPresent();
        assertThat(outcome.metrics().get()).containsEntry("executionTimeMs", 150);
        assertThat(outcome.metrics().get()).containsEntry("memoryUsed", 1024);
    }

    /**
     * Verifies that the builder can add metrics both individually and in bulk.
     * Tests the withMetrics method for adding multiple metrics at once.
     */
    @Test
    void shouldCreateOutcome_whenUsingBuilderWithMetrics() {
        // Arrange
        Map<String, Object> additionalMetrics = Map.of("cpuUsage", 0.75);

        // Act
        ActionOutcome outcome = ActionOutcome.builder(ActionOutcome.OutcomeStatus.PASS)
                .withMetrics(additionalMetrics)
                .withMetric("executionTimeMs", 100)
                .build();

        // Assert
        assertThat(outcome.metrics()).isPresent();
        assertThat(outcome.metrics().get()).containsEntry("cpuUsage", 0.75);
        assertThat(outcome.metrics().get()).containsEntry("executionTimeMs", 100);
    }

    /**
     * Verifies that when metrics is null in the constructor, it is automatically
     * converted to an empty Optional for consistency.
     */
    @Test
    void shouldUseEmptyOptional_whenMetricsIsNull() {
        // Act
        ActionOutcome outcome = new ActionOutcome(
                ActionOutcome.OutcomeStatus.PASS,
                null,
                null
        );

        // Assert
        assertThat(outcome.metrics()).isEmpty();
    }

    /**
     * Verifies that creating an outcome with a null status throws an IllegalArgumentException.
     * Status is required to determine the outcome level.
     */
    @Test
    void shouldThrowException_whenStatusIsNull() {
        // Act & Assert
        assertThatThrownBy(() -> new ActionOutcome(null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("OutcomeStatus cannot be null");
    }

    /**
     * Verifies that getDataAs can convert data to a Map when the data is already a Map.
     * This tests the type-safe conversion method for retrieving data in a specific format.
     */
    @Test
    void shouldConvertDataToMap_whenDataIsMap() {
        // Arrange
        Map<String, Object> data = new HashMap<>();
        data.put("key", "value");
        ActionOutcome outcome = ActionOutcome.of(ActionOutcome.OutcomeStatus.PASS, data);

        // Act
        var result = outcome.getDataAs(Map.class);

        // Assert
        assertThat(result).isPresent();
        @SuppressWarnings("unchecked")
        Map<String, Object> map = result.get();
        assertThat(map).containsEntry("key", "value");
    }

    /**
     * Verifies that getDataAs returns an empty Optional when data is not present.
     * This ensures the method handles missing data gracefully.
     */
    @Test
    void shouldReturnEmpty_whenDataIsNotPresent() {
        // Arrange
        ActionOutcome outcome = ActionOutcome.of(ActionOutcome.OutcomeStatus.PASS);

        // Act
        var result = outcome.getDataAs(Map.class);

        // Assert
        assertThat(result).isEmpty();
    }

    /**
     * Verifies that getDataAs returns data directly when it's already of the requested type.
     * No conversion is needed in this case, improving performance.
     */
    @Test
    void shouldReturnDirectly_whenDataIsAlreadyOfRequestedType() {
        // Arrange
        String data = "test-string";
        ActionOutcome outcome = ActionOutcome.of(ActionOutcome.OutcomeStatus.PASS, data);

        // Act
        var result = outcome.getDataAs(String.class);

        // Assert
        assertThat(result).contains("test-string");
    }

    /**
     * Verifies that getDataAs can convert data using Jackson ObjectMapper when needed.
     * Tests the conversion path for data that requires transformation.
     */
    @Test
    void shouldConvertData_whenDataNeedsConversion() {
        // Arrange
        Map<String, Object> data = new HashMap<>();
        data.put("name", "test");
        data.put("value", 42);
        ActionOutcome outcome = ActionOutcome.of(ActionOutcome.OutcomeStatus.PASS, data);

        // Act
        var result = outcome.getDataAs(Map.class);

        // Assert
        assertThat(result).isPresent();
        @SuppressWarnings("unchecked")
        Map<String, Object> map = result.get();
        assertThat(map).containsEntry("name", "test");
        assertThat(map).containsEntry("value", 42);
    }

    /**
     * Verifies that getDataAs throws an IllegalArgumentException when conversion fails.
     * This ensures invalid conversions are caught early with a clear error message.
     */
    @Test
    void shouldThrowException_whenConversionFails() {
        // Arrange
        String data = "not-a-number";
        ActionOutcome outcome = ActionOutcome.of(ActionOutcome.OutcomeStatus.PASS, data);

        // Act & Assert
        assertThatThrownBy(() -> outcome.getDataAs(Integer.class))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Failed to convert");
    }

    /**
     * Verifies that OutcomeStatus.fromString correctly parses valid status strings.
     * Tests all three status values: PASS, WARN, and ERROR.
     */
    @Test
    void shouldParseStatusFromString_whenValidStatus() {
        // Act & Assert
        assertThat(ActionOutcome.OutcomeStatus.fromString("PASS")).isEqualTo(ActionOutcome.OutcomeStatus.PASS);
        assertThat(ActionOutcome.OutcomeStatus.fromString("WARN")).isEqualTo(ActionOutcome.OutcomeStatus.WARN);
        assertThat(ActionOutcome.OutcomeStatus.fromString("ERROR")).isEqualTo(ActionOutcome.OutcomeStatus.ERROR);
    }

    /**
     * Verifies that OutcomeStatus.fromString is case-insensitive.
     * This allows flexible input formats (e.g., "pass", "Pass", "PASS" all work).
     */
    @Test
    void shouldParseStatusFromString_whenCaseInsensitive() {
        // Act & Assert
        assertThat(ActionOutcome.OutcomeStatus.fromString("pass")).isEqualTo(ActionOutcome.OutcomeStatus.PASS);
        assertThat(ActionOutcome.OutcomeStatus.fromString("Pass")).isEqualTo(ActionOutcome.OutcomeStatus.PASS);
        assertThat(ActionOutcome.OutcomeStatus.fromString("PASS")).isEqualTo(ActionOutcome.OutcomeStatus.PASS);
    }

    /**
     * Verifies that OutcomeStatus.fromString returns ERROR as a default when
     * the input is invalid or null. This provides safe fallback behavior.
     */
    @Test
    void shouldReturnError_whenStatusStringIsInvalid() {
        // Act & Assert
        assertThat(ActionOutcome.OutcomeStatus.fromString("INVALID")).isEqualTo(ActionOutcome.OutcomeStatus.ERROR);
        assertThat(ActionOutcome.OutcomeStatus.fromString(null)).isEqualTo(ActionOutcome.OutcomeStatus.ERROR);
    }

    /**
     * Verifies that each OutcomeStatus returns the correct numeric level.
     * Levels are: ERROR=0, WARN=1, PASS=2. Higher levels indicate better outcomes.
     */
    @Test
    void shouldReturnCorrectLevel_whenGettingStatusLevel() {
        // Act & Assert
        assertThat(ActionOutcome.OutcomeStatus.ERROR.getLevel()).isEqualTo(0);
        assertThat(ActionOutcome.OutcomeStatus.WARN.getLevel()).isEqualTo(1);
        assertThat(ActionOutcome.OutcomeStatus.PASS.getLevel()).isEqualTo(2);
    }
}
