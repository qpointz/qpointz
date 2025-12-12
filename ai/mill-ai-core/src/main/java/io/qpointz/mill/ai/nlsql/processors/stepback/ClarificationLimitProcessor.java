package io.qpointz.mill.ai.nlsql.processors.stepback;

import io.qpointz.mill.ai.chat.ChatCallPostProcessor;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Trims clarification questions to a maximum count.
 */
@RequiredArgsConstructor
public class ClarificationLimitProcessor implements ChatCallPostProcessor {

    /**
     * Maximum number of clarification questions to retain.
     */
    private final int maxQuestions;

    /**
     * Truncates the questions list to the configured maximum while preserving order.
     */
    @Override
    public Map<String, Object> process(Map<String, Object> result) {
        val normalized = new LinkedHashMap<>(Optional.ofNullable(result).orElseGet(LinkedHashMap::new));
        val questions = normalized.get("questions");
        if (questions instanceof Iterable<?> iterable) {
            val limited = new ArrayList<>();
            for (Object question : iterable) {
                if (limited.size() >= maxQuestions) {
                    break;
                }
                limited.add(question);
            }
            normalized.put("questions", limited);
        }
        return normalized;
    }
}
