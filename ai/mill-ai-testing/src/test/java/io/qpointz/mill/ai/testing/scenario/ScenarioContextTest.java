package io.qpointz.mill.ai.testing.scenario;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for the {@link ScenarioContext} abstract class.
 * Tests result management, serialization to JSON/YAML, and validation.
 */
class ScenarioContextTest {

    private TestScenarioContext context;

    @BeforeEach
    void setUp() {
        context = new TestScenarioContext();
    }

    /**
     * Verifies that a valid ActionResult can be added to the context.
     * The result should be stored and accessible via getResults().
     */
    @Test
    void shouldAddResult_whenResultIsValid() {
        // Arrange
        Action action = new Action("do-get", Map.of());
        ActionOutcome outcome = ActionOutcome.of(ActionOutcome.OutcomeStatus.PASS);
        ActionResult result = new ActionResult(action, true, java.util.Optional.empty(), outcome);

        // Act
        context.addResult(result);

        // Assert
        assertThat(context.getResultCount()).isEqualTo(1);
        assertThat(context.getResults()).containsExactly(result);
    }

    /**
     * Verifies that multiple results can be added to the context.
     * Results should be stored in order and all be accessible.
     */
    @Test
    void shouldAddMultipleResults_whenMultipleResultsAdded() {
        // Arrange
        Action action1 = new Action("do-get", Map.of());
        Action action2 = new Action("do-ask", Map.of());
        ActionResult result1 = new ActionResult(action1, true, java.util.Optional.empty(),
                ActionOutcome.of(ActionOutcome.OutcomeStatus.PASS));
        ActionResult result2 = new ActionResult(action2, true, java.util.Optional.empty(),
                ActionOutcome.of(ActionOutcome.OutcomeStatus.PASS));

        // Act
        context.addResult(result1);
        context.addResult(result2);

        // Assert
        assertThat(context.getResultCount()).isEqualTo(2);
        assertThat(context.getResults()).containsExactly(result1, result2);
    }

    /**
     * Verifies that getResults() returns an unmodifiable list.
     * This prevents external modification of the internal results collection.
     */
    @Test
    void shouldReturnUnmodifiableList_whenGettingResults() {
        // Arrange
        Action action = new Action("do-get", Map.of());
        ActionResult result = new ActionResult(action, true, java.util.Optional.empty(),
                ActionOutcome.of(ActionOutcome.OutcomeStatus.PASS));
        context.addResult(result);

        // Act & Assert
        List<ActionResult> results = context.getResults();
        assertThatThrownBy(() -> results.add(result))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    /**
     * Verifies that getLastResult() returns null when no results have been added.
     * This provides safe access when the context is empty.
     */
    @Test
    void shouldReturnNull_whenNoResultsExist() {
        // Act
        ActionResult lastResult = context.getLastResult();

        // Assert
        assertThat(lastResult).isNull();
    }

    /**
     * Verifies that getLastResult() returns the most recently added result.
     * This is useful for accessing the latest action outcome.
     */
    @Test
    void shouldReturnLastResult_whenResultsExist() {
        // Arrange
        Action action1 = new Action("do-get", Map.of());
        Action action2 = new Action("do-ask", Map.of());
        ActionResult result1 = new ActionResult(action1, true, java.util.Optional.empty(),
                ActionOutcome.of(ActionOutcome.OutcomeStatus.PASS));
        ActionResult result2 = new ActionResult(action2, true, java.util.Optional.empty(),
                ActionOutcome.of(ActionOutcome.OutcomeStatus.PASS));
        context.addResult(result1);
        context.addResult(result2);

        // Act
        ActionResult lastResult = context.getLastResult();

        // Assert
        assertThat(lastResult).isEqualTo(result2);
    }

    /**
     * Verifies that adding a null result throws an IllegalArgumentException.
     * This prevents null values from corrupting the results collection.
     */
    @Test
    void shouldThrowException_whenAddingNullResult() {
        // Act & Assert
        assertThatThrownBy(() -> context.addResult(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ActionResult cannot be null");
    }

    /**
     * Verifies that the context can be serialized to JSON format.
     * The JSON should contain action and outcome information.
     */
    @Test
    void shouldSerializeToJson_whenContextHasResults() throws IOException {
        // Arrange
        Action action = new Action("do-get", Map.of());
        ActionResult result = new ActionResult(action, true, java.util.Optional.empty(),
                ActionOutcome.of(ActionOutcome.OutcomeStatus.PASS));
        context.addResult(result);

        // Act
        String json = context.toJson();

        // Assert
        assertThat(json).isNotEmpty();
        assertThat(json).contains("do-get");
        assertThat(json).contains("PASS");
    }

    /**
     * Verifies that the context can be serialized to YAML format.
     * The YAML should contain action and outcome information in a human-readable format.
     */
    @Test
    void shouldSerializeToYaml_whenContextHasResults() throws IOException {
        // Arrange
        Action action = new Action("do-get", Map.of());
        ActionResult result = new ActionResult(action, true, java.util.Optional.empty(),
                ActionOutcome.of(ActionOutcome.OutcomeStatus.PASS));
        context.addResult(result);

        // Act
        String yaml = context.toYaml();

        // Assert
        assertThat(yaml).isNotEmpty();
        assertThat(yaml).contains("do-get");
    }

    /**
     * Verifies that the context can be serialized to JSON format via an OutputStream.
     * This allows writing JSON directly to files or network streams.
     */
    @Test
    void shouldSerializeToJsonOutputStream_whenContextHasResults() throws IOException {
        // Arrange
        Action action = new Action("do-get", Map.of());
        ActionResult result = new ActionResult(action, true, java.util.Optional.empty(),
                ActionOutcome.of(ActionOutcome.OutcomeStatus.PASS));
        context.addResult(result);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // Act
        context.serializeToJson(outputStream);

        // Assert
        String json = outputStream.toString();
        assertThat(json).isNotEmpty();
        assertThat(json).contains("do-get");
    }

    /**
     * Verifies that the context can be serialized to YAML format via an OutputStream.
     * This allows writing YAML directly to files or network streams.
     */
    @Test
    void shouldSerializeToYamlOutputStream_whenContextHasResults() throws IOException {
        // Arrange
        Action action = new Action("do-get", Map.of());
        ActionResult result = new ActionResult(action, true, java.util.Optional.empty(),
                ActionOutcome.of(ActionOutcome.OutcomeStatus.PASS));
        context.addResult(result);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // Act
        context.serializeToYaml(outputStream);

        // Assert
        String yaml = outputStream.toString();
        assertThat(yaml).isNotEmpty();
        assertThat(yaml).contains("do-get");
    }
}
