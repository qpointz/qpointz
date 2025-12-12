package io.qpointz.mill.ai.nlsql.processors.stepback;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class StepBackPostProcessorsTest {

    @Test
    void syncsClarificationWhenAmbiguitiesPresent() {
        var stepBack = new LinkedHashMap<String, Object>();
        stepBack.put("ambiguities", List.of("time_range"));
        var input = new LinkedHashMap<String, Object>();
        input.put("step-back", stepBack);

        Map<String, Object> result = new SyncClarificationFlagProcessor().process(input);
        assertTrue(Boolean.parseBoolean(result.get("need-clarification").toString()));
        Map<?, ?> updatedStepBack = (Map<?, ?>) result.get("step-back");
        assertEquals(true, updatedStepBack.get("needs-clarification"));
    }

    @Test
    void reasoningIdDefaultsToNewWhenClarificationIsNeeded() {
        var input = Map.<String, Object>of(
                "need-clarification", true,
                "reasoning-id", ""
        );
        Map<String, Object> result = new ReasoningIdActionProcessor().process(input);
        assertDoesNotThrow(()-> UUID.fromString(result.get("reasoning-id").toString()));
        assertEquals(true, result.get("need-clarification"));
    }

    @Test
    void reasoningIdResetsWhenClarificationResolved() {
        var input = Map.<String, Object>of(
                "need-clarification", false,
                "reasoning-id", "new"
        );
        Map<String, Object> result = new ReasoningIdActionProcessor().process(input);
        assertDoesNotThrow(()-> UUID.fromString(result.get("reasoning-id").toString()));
        assertEquals(false, result.get("need-clarification"));
    }

    @Test
    void limitsClarificationQuestionsToMax() {
        var questions = new ArrayList<Map<String, Object>>();
        questions.add(Map.of("id", "q1"));
        questions.add(Map.of("id", "q2"));
        questions.add(Map.of("id", "q3"));
        questions.add(Map.of("id", "q4"));
        var input = new LinkedHashMap<String, Object>();
        input.put("questions", questions);

        Map<String, Object> result = new ClarificationLimitProcessor(3).process(input);
        List<?> limited = (List<?>) result.get("questions");
        assertEquals(3, limited.size());
    }

    @Test
    void normalizesMissingStructures() {
        Map<String, Object> result = new NormalizeStepBackProcessor().process(new LinkedHashMap<>());
        assertNotNull(result.get("step-back"));
        assertNotNull(result.get("questions"));
        assertNotNull(result.get("metadata-gaps"));
        assertEquals(false, result.get("need-clarification"));
    }
}
