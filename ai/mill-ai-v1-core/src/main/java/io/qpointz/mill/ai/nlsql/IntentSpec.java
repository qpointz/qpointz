package io.qpointz.mill.ai.nlsql;

import io.qpointz.mill.ai.chat.ChatCall;
import io.qpointz.mill.ai.chat.ChatCallPostProcessor;
import io.qpointz.mill.ai.nlsql.models.ReasoningResponse;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.function.Function;

@AllArgsConstructor
@Builder
public final class IntentSpec {

    @Getter
    private final String key;

    @Getter(AccessLevel.PRIVATE)
    private final Function<ReasoningResponse, ChatCall> callFunc;

    @Getter(AccessLevel.PRIVATE)
    private final Function<ReasoningResponse, List<ChatCallPostProcessor>> postProcessorFunc;

    public ChatCall getCall(ReasoningResponse response) {
        return this.callFunc.apply(response);
    }

    public List<ChatCallPostProcessor> getPostProcessors(ReasoningResponse response) {
        return this.postProcessorFunc == null
                ? List.of()
                : this.postProcessorFunc.apply(response);
    }

    public static IntentSpecBuilder builder(String key) {
        return new IntentSpecBuilder()
                .key(key);
    }

}
