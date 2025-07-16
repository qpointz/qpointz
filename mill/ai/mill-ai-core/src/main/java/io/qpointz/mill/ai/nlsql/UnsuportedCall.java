package io.qpointz.mill.ai.nlsql;

import io.qpointz.mill.ai.chat.ChatCall;
import io.qpointz.mill.ai.nlsql.models.ReasoningResponse;
import io.qpointz.mill.ai.nlsql.processors.RetainReasoningProcessor;
import lombok.AllArgsConstructor;

import java.util.Map;

@AllArgsConstructor
public class UnsuportedCall implements ChatCall {

    private final ReasoningResponse reasoningResponse;

    private static final Map<String, Object> RAW_RESPONSE = Map.of(
            "intent", IntentSpecs.UNSUPPORTED_INTENT_KEY);

    @Override
    public Map<String, Object> asMap() {
        return new RetainReasoningProcessor(reasoningResponse)
                .process(RAW_RESPONSE);
    }
}
