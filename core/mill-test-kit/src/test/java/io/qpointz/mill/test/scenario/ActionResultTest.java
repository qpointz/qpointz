package io.qpointz.mill.test.scenario;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for the {@link ActionResult} record class.
 * Tests construction, validation, and field handling.
 */
class ActionResultTest {

    /**
     * Verifies that an ActionResult can be created with all valid parameters:
     * action, success flag, optional error message, and outcome.
     */
    @Test
    void shouldCreateResult_whenValidParameters() {
        // Arrange
        Action action = new Action("do-get", Map.of());
        ActionOutcome outcome = ActionOutcome.of(ActionOutcome.OutcomeStatus.PASS);

        // Act
        ActionResult result = new ActionResult(action, true, Optional.empty(), outcome);

        // Assert
        assertThat(result.action()).isEqualTo(action);
        assertThat(result.success()).isTrue();
        assertThat(result.errorMessage()).isEmpty();
        assertThat(result.outcome()).isEqualTo(outcome);
    }

    /**
     * Verifies that when errorMessage is null, the ActionResult constructor
     * automatically converts it to an empty Optional for consistency.
     */
    @Test
    void shouldUseEmptyOptional_whenErrorMessageIsNull() {
        // Arrange
        Action action = new Action("do-get", Map.of());
        ActionOutcome outcome = ActionOutcome.of(ActionOutcome.OutcomeStatus.PASS);

        // Act
        ActionResult result = new ActionResult(action, true, null, outcome);

        // Assert
        assertThat(result.errorMessage()).isEmpty();
    }

    /**
     * Verifies that creating an ActionResult with a null action throws an IllegalArgumentException.
     * The action is required to identify what was executed.
     */
    @Test
    void shouldThrowException_whenActionIsNull() {
        // Arrange
        ActionOutcome outcome = ActionOutcome.of(ActionOutcome.OutcomeStatus.PASS);

        // Act & Assert
        assertThatThrownBy(() -> new ActionResult(null, true, Optional.empty(), outcome))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Action cannot be null");
    }

    /**
     * Verifies that creating an ActionResult with a null outcome throws an IllegalArgumentException.
     * The outcome is required to record the execution result.
     */
    @Test
    void shouldThrowException_whenOutcomeIsNull() {
        // Arrange
        Action action = new Action("do-get", Map.of());

        // Act & Assert
        assertThatThrownBy(() -> new ActionResult(action, true, Optional.empty(), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ActionOutcome cannot be null");
    }
}
