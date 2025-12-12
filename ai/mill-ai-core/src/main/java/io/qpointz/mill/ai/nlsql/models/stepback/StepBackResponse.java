package io.qpointz.mill.ai.nlsql.models.stepback;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.val;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Top-level Step-Back response envelope returned by the LLM.
 * Contains structured summary, clarification status, reasoning-id action, questions, and metadata gaps.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record StepBackResponse(
        /**
         * Original user query echoed for downstream consumers.
         */
        @JsonProperty("query") String query,
        /**
         * Structured Step-Back analysis block.
         */
        @JsonProperty("step-back") StepBackSummary stepBack,
        /**
         * Top-level clarification flag.
         */
        @JsonProperty("need-clarification") boolean needClarification,
        /**
         * Action keyword instructing post-processing on how to handle reasoning-id.
         */
        @JsonProperty("reasoning-id") String reasoningId,
        /**
         * Clarification questions produced by Step-Back.
         */
        @JsonProperty("questions") List<ClarificationQuestion> questions,
        /**
         * Metadata gaps detected during Step-Back (align with enrich-model structures).
         */
        @JsonProperty("metadata-gaps") List<String> metadataGaps
) {

    /**
        * Safe accessor returning an empty list when questions are missing.
        */
    public List<ClarificationQuestion> questionsSafe() {
        return Optional.ofNullable(questions).orElse(Collections.emptyList());
    }

    /**
     * Safe accessor returning an empty list when metadata gaps are missing.
     */
    public List<String> metadataGapsSafe() {
        return Optional.ofNullable(metadataGaps).orElse(Collections.emptyList());
    }

    public static StepBackResponse empty() {
        val sum = new StepBackSummary("", List.of(),List.of(),List.of(),List.of(),false);
        return new StepBackResponse("", sum, false, "",List.of(),List.of());
    }

}
