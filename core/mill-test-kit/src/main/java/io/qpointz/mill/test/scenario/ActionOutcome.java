package io.qpointz.mill.test.scenario;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Outcome of executing an action.
 * Contains the status level, optional result data, and metrics metadata.
 */
public record ActionOutcome(
        /**
         * The status level of the outcome.
         */
        OutcomeStatus status,

        /**
         * Optional result data from the action execution.
         * Can be any serializable object (Map, List, custom object, etc.).
         * The structure depends on the action type.
         */
        Optional<Object> data,

        /**
         * Optional metrics metadata recorded during action execution.
         * Can include execution time, resource usage, performance metrics, etc.
         */
        Optional<Map<String, Object>> metrics
) {
    public ActionOutcome {
        if (status == null) {
            throw new IllegalArgumentException("OutcomeStatus cannot be null");
        }
        if (data == null) {
            data = Optional.empty();
        }
        if (metrics == null) {
            metrics = Optional.empty();
        }
    }

    /**
     * Creates an outcome with the given status and no data or metrics.
     *
     * @param status the outcome status
     * @return an ActionOutcome with the given status
     */
    public static ActionOutcome of(OutcomeStatus status) {
        return new ActionOutcome(status, Optional.empty(), Optional.empty());
    }

    /**
     * Creates an outcome with the given status and data.
     *
     * @param status the outcome status
     * @param data the result data (should be serializable)
     * @return an ActionOutcome with the given status and data
     */
    public static ActionOutcome of(OutcomeStatus status, Object data) {
        if (data == null) {
            return new ActionOutcome(status, Optional.empty(), Optional.empty());
        }
        return new ActionOutcome(status, Optional.of(data), Optional.empty());
    }

    /**
     * Creates an outcome with the given status, data, and metrics.
     *
     * @param status the outcome status
     * @param data the result data (should be serializable)
     * @param metrics the metrics metadata
     * @return an ActionOutcome with the given status, data, and metrics
     */
    public static ActionOutcome of(OutcomeStatus status, Object data, Map<String, Object> metrics) {
        Optional<Map<String, Object>> metricsOpt = metrics != null ? Optional.of(metrics) : Optional.empty();
        if (data == null) {
            return new ActionOutcome(status, Optional.empty(), metricsOpt);
        }
        return new ActionOutcome(status, Optional.of(data), metricsOpt);
    }

    /**
     * Creates an outcome with the given status and metrics (no data).
     *
     * @param status the outcome status
     * @param metrics the metrics metadata
     * @return an ActionOutcome with the given status and metrics
     */
    public static ActionOutcome ofMetrics(OutcomeStatus status, Map<String, Object> metrics) {
        Optional<Map<String, Object>> metricsOpt = metrics != null ? Optional.of(metrics) : Optional.empty();
        return new ActionOutcome(status, Optional.empty(), metricsOpt);
    }

    /**
     * Creates a builder for constructing ActionOutcome with metrics.
     *
     * @param status the outcome status
     * @return a new ActionOutcomeBuilder
     */
    public static ActionOutcomeBuilder builder(OutcomeStatus status) {
        return new ActionOutcomeBuilder(status);
    }

    /**
     * Converts the data to an instance of the specified type.
     * Uses Jackson's ObjectMapper to perform the conversion.
     * If data is not present, returns empty Optional.
     * If data is already of the requested type, returns it directly.
     * Otherwise, attempts to convert using Jackson.
     *
     * @param <T> the target type
     * @param type the target class
     * @return Optional containing the converted data, or empty if data is not present
     * @throws IllegalArgumentException if conversion fails
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<T> getDataAs(Class<T> type) {
        if (data.isEmpty()) {
            return Optional.empty();
        }

        Object value = data.get();

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
                    "Failed to convert data to " + type.getSimpleName() + ": " + e.getMessage(), e);
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

    /**
     * Builder for constructing ActionOutcome with metrics.
     */
    public static class ActionOutcomeBuilder {
        private final OutcomeStatus status;
        private Object data;
        private final Map<String, Object> metrics = new HashMap<>();

        private ActionOutcomeBuilder(OutcomeStatus status) {
            this.status = status;
        }

        /**
         * Sets the result data.
         *
         * @param data the result data (should be serializable)
         * @return this builder
         */
        public ActionOutcomeBuilder withData(Object data) {
            this.data = data;
            return this;
        }

        /**
         * Adds a metric.
         *
         * @param key the metric key
         * @param value the metric value
         * @return this builder
         */
        public ActionOutcomeBuilder withMetric(String key, Object value) {
            this.metrics.put(key, value);
            return this;
        }

        /**
         * Adds multiple metrics.
         *
         * @param metrics the metrics to add
         * @return this builder
         */
        public ActionOutcomeBuilder withMetrics(Map<String, Object> metrics) {
            this.metrics.putAll(metrics);
            return this;
        }

        /**
         * Builds the ActionOutcome.
         *
         * @return the constructed ActionOutcome
         */
        public ActionOutcome build() {
            Optional<Map<String, Object>> metricsOpt = metrics.isEmpty() ? Optional.empty() : Optional.of(new HashMap<>(metrics));
            return new ActionOutcome(
                    status,
                    data != null ? Optional.of(data) : Optional.empty(),
                    metricsOpt
            );
        }
    }

    /**
     * Status levels for action outcomes.
     * Higher numeric values indicate better outcomes.
     */
    public enum OutcomeStatus {
        /**
         * Error status (level 0) - action failed or encountered an error.
         */
        ERROR(0),

        /**
         * Warning status (level 1) - action completed but with warnings.
         */
        WARN(1),

        /**
         * Pass status (level 2) - action completed successfully.
         */
        PASS(2);

        private final int level;

        OutcomeStatus(int level) {
            this.level = level;
        }

        /**
         * Gets the numeric level of this status.
         *
         * @return the status level
         */
        public int getLevel() {
            return this.level;
        }

        /**
         * Parses a status from a string (case-insensitive).
         *
         * @param status the status string
         * @return the OutcomeStatus, or ERROR if not recognized
         */
        public static OutcomeStatus fromString(String status) {
            if (status == null) {
                return ERROR;
            }
            try {
                return OutcomeStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ERROR;
            }
        }
    }
}

