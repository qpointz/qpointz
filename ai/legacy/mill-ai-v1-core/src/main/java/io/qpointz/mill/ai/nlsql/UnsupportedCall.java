package io.qpointz.mill.ai.nlsql;

import io.qpointz.mill.ai.chat.ChatCall;
import io.qpointz.mill.ai.chat.ChatCallResponse;
import io.qpointz.mill.ai.nlsql.models.ReasoningResponse;
import io.qpointz.mill.ai.nlsql.processors.RetainReasoningProcessor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.ai.chat.model.ChatResponse;

import java.util.Map;
import java.util.Optional;

@AllArgsConstructor
public class UnsupportedCall implements ChatCall {

    private final ReasoningResponse reasoningResponse;

    private static final Map<String, Object> RAW_RESPONSE = Map.of(
            "intent", IntentSpecs.UNSUPPORTED_INTENT_KEY);

    @Override
    public Map<String, Object> asMap() {
        return new RetainReasoningProcessor(reasoningResponse)
                .process(RAW_RESPONSE);
    }

    @AllArgsConstructor
    class UnsupportedCallResponse implements ChatCallResponse {

        @Getter
        private Map<String,Object> content;

        @Override
        public Optional<ChatResponse> getResponse() {
            return null;
        }

        @Override
        public Map<String, Object> contentAsMap() {
            return Map.of();
        }

    }

    @Override
    public ChatCallResponse call() {
        return null;
    }
}
