package io.qpointz.mill.ai.nlsql.processors.stepback;

import io.qpointz.mill.ai.chat.ChatCallPostProcessor;
import lombok.val;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Ensures Step-Back responses have the expected structure with defaults.
 * Normalizes nested maps/lists, mirrors needs-clarification, and fills missing collections.
 */
public class NormalizeStepBackProcessor implements ChatCallPostProcessor {

    /**
     * Converts a value to a mutable map, falling back to an empty map.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> mapValue(Object value) {
        if (value instanceof Map<?, ?> raw) {
            return new LinkedHashMap<>((Map<String, Object>) raw);
        }
        return new LinkedHashMap<>();
    }

    /**
     * Converts an iterable/array value to a mutable list, or empty list when absent.
     */
    @SuppressWarnings("unchecked")
    private ArrayList<Object> listValue(Object value) {
        if (value instanceof Iterable<?> iterable) {
            val list = new ArrayList<>();
            iterable.forEach(list::add);
            return list;
        }
        if (value instanceof Object[] arr) {
            val list = new ArrayList<>();
            for (Object o : arr) {
                list.add(o);
            }
            return list;
        }
        return new ArrayList<>();
    }

    /**
     * Normalizes the raw LLM map into a predictable Step-Back shape with defaults.
     */
    @Override
    public Map<String, Object> process(Map<String, Object> result) {
        val normalized = new LinkedHashMap<>(Optional.ofNullable(result).orElseGet(LinkedHashMap::new));
        val stepBack = mapValue(normalized.get("step-back"));

        boolean needsClarification = Optional.ofNullable(stepBack.get("needs-clarification"))
                .map(Object::toString)
                .map(Boolean::parseBoolean)
                .orElse(false);

        stepBack.putIfAbsent("abstract-task", "");
        stepBack.putIfAbsent("core-concepts", listValue(stepBack.get("core-concepts")));
        stepBack.putIfAbsent("required-concepts", listValue(stepBack.get("required-concepts")));
        stepBack.putIfAbsent("required-relations", listValue(stepBack.get("required-relations")));
        stepBack.putIfAbsent("ambiguities", listValue(stepBack.get("ambiguities")));
        stepBack.put("needs-clarification", needsClarification);

        normalized.put("step-back", stepBack);
        normalized.putIfAbsent("need-clarification", needsClarification);
        normalized.putIfAbsent("questions", listValue(normalized.get("questions")));
        normalized.putIfAbsent("metadata-gaps", listValue(normalized.get("metadata-gaps")));
        return normalized;
    }
}
