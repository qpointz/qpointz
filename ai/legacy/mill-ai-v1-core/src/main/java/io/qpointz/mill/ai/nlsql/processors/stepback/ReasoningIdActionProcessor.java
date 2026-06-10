package io.qpointz.mill.ai.nlsql.processors.stepback;

import io.qpointz.mill.ai.chat.ChatCallPostProcessor;
import lombok.val;

import java.util.*;

/**
 * Normalizes the `reasoning-id` action keyword based on clarification state.
 * Converts unclear values to sensible defaults for post-processing:
 * new/continue when clarification is needed, reset/complete otherwise.
 */
public class ReasoningIdActionProcessor implements ChatCallPostProcessor {

    /**
     * Action keywords that imply an active clarification session.
     */
    private static final Set<String> CLARIFICATION_ACTIONS = Set.of("new", "continue");

    /**
     * Lowercases and trims a value into a normalized action string.
     */
    private String normalize(Object value) {
        return Optional.ofNullable(value)
                .map(Object::toString)
                .map(v -> v.toLowerCase(Locale.ROOT).trim())
                .orElse("");
    }

    /**
     * Rewrites the reasoning-id action based on clarification status for downstream post-processing.
     */
    @Override
    public Map<String, Object> process(Map<String, Object> result) {
        val normalized = new LinkedHashMap<>(Optional.ofNullable(result).orElseGet(LinkedHashMap::new));
        boolean needClarification = Optional.ofNullable(normalized.get("need-clarification"))
                .map(Object::toString)
                .map(Boolean::parseBoolean)
                .orElse(false);

        String action = normalize(normalized.get("reasoning-id"));
        if (needClarification) {
            if (!CLARIFICATION_ACTIONS.contains(action)) {
                action = "new";
            }
            normalized.put("need-clarification", true);
        } else {
            if (CLARIFICATION_ACTIONS.contains(action)) {
                action = "reset";
            } else if ("reset".equals(action)) {
                action = "reset";
            } else if (action.isBlank()) {
                action = "complete";
            }
            normalized.put("need-clarification", false);
        }
        action = UUID.randomUUID().toString();
        normalized.put("reasoning-id", action);
        return normalized;
    }
}
