package io.qpointz.mill.ai.nlsql.models.stepback;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Canonical structure for the Step-Back summary emitted by the LLM.
 * Captures abstraction, domain concepts, relations, ambiguities, and gating flag.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record StepBackSummary(
        /**
         * High-level reformulation of the user request.
         */
        @JsonProperty("abstract-task") String abstractTask,
        /**
         * Concepts explicitly or implicitly mentioned by the user.
         */
        @JsonProperty("core-concepts") List<String> coreConcepts,
        /**
         * Domain entities required to satisfy the request.
         */
        @JsonProperty("required-concepts") List<String> requiredConcepts,
        /**
         * Relationships between entities needed to interpret the query.
         */
        @JsonProperty("required-relations") List<String> requiredRelations,
        /**
         * Underspecified or ambiguous points blocking execution.
         */
        @JsonProperty("ambiguities") List<String> ambiguities,
        /**
         * True when ambiguities or gaps require clarification.
         */
        @JsonProperty("needs-clarification") Boolean needsClarification
) {
}
