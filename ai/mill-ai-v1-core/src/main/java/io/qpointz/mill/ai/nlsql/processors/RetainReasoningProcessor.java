package io.qpointz.mill.ai.nlsql.processors;

import io.qpointz.mill.ai.chat.ChatCallPostProcessor;
import io.qpointz.mill.ai.nlsql.models.ReasoningResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@AllArgsConstructor
public final class RetainReasoningProcessor implements ChatCallPostProcessor {


    public static final String REASONING_KEY = "reasoning";


    @Getter
    private final ReasoningResponse reason;

    @Override
    public Map<String, Object> process(Map<String, Object> result) {
        val m = new HashMap<>(result);
        if (!result.containsKey(REASONING_KEY)) {
            log.warn("Response already contains reasoning information {}: Skipping postprocessing.", result.get(REASONING_KEY));
            m.put(REASONING_KEY, reason);
        }

        m.putIfAbsent("resultIntent", reason.intent());

        return m;
    }
}
