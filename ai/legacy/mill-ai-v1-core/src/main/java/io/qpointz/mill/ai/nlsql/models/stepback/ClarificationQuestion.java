package io.qpointz.mill.ai.nlsql.models.stepback;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Clarification question schema returned by Step-Back.
 * Includes stable id, user-facing question, and optional expected type hint.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ClarificationQuestion(
        /**
         * Stable identifier for the clarification slot.
         */
        @JsonProperty("id") String id,
        /**
         * User-facing question text.
         */
        @JsonProperty("question") String question,
        /**
         * Optional hint about the expected answer type.
         */
        @JsonProperty("expectedType") String expectedType
) {
}
