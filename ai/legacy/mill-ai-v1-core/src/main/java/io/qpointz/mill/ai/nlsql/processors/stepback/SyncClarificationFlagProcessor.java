package io.qpointz.mill.ai.nlsql.processors.stepback;

import io.qpointz.mill.ai.chat.ChatCallPostProcessor;
import lombok.val;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Aligns clarification flags across the top-level payload and the step-back block.
 * If ambiguities or questions are present, forces need-clarification=true.
 */
public class SyncClarificationFlagProcessor implements ChatCallPostProcessor {

    /**
     * Ensures clarification flags are consistent between top-level and step-back structures.
     */
    @Override
    public Map<String, Object> process(Map<String, Object> result) {
        val normalized = new LinkedHashMap<>(Optional.ofNullable(result).orElseGet(LinkedHashMap::new));
        val stepBackRaw = normalized.getOrDefault("step-back", Map.of());
        Map<String, Object> stepBackNormalized = new LinkedHashMap<>();
        if (stepBackRaw instanceof Map<?, ?> stepBackMap) {
            stepBackMap.forEach((k, v) -> stepBackNormalized.put(String.valueOf(k), v));
        }

        boolean stepBackNeedsClarification = false;
        if (!stepBackNormalized.isEmpty()) {
            stepBackNeedsClarification = Optional.ofNullable(stepBackNormalized.get("needs-clarification"))
                    .map(Object::toString)
                    .map(Boolean::parseBoolean)
                    .orElse(false);
            val ambiguities = stepBackNormalized.get("ambiguities");
            if (ambiguities instanceof Iterable<?> iterable && iterable.iterator().hasNext()) {
                stepBackNeedsClarification = true;
            }
        }

        boolean questionsPresent = false;
        val questions = normalized.get("questions");
        if (questions instanceof Iterable<?> iterable && iterable.iterator().hasNext()) {
            questionsPresent = true;
        }

        if (stepBackNeedsClarification || questionsPresent) {
            normalized.put("need-clarification", true);
            stepBackNormalized.put("needs-clarification", true);
        }

        normalized.put("step-back", stepBackNormalized);
        return normalized;
    }
}
