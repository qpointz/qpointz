package io.qpointz.mill.ai.nlsql.stepback;

import io.qpointz.mill.ai.chat.ChatCall;
import io.qpointz.mill.ai.chat.ChatClientBuilder;
import io.qpointz.mill.ai.chat.messages.MessageList;
import io.qpointz.mill.ai.chat.messages.MessageSelector;
import io.qpointz.mill.ai.nlsql.ChatCallBase;
import io.qpointz.mill.ai.nlsql.PostProcessors;
import io.qpointz.mill.ai.nlsql.processors.stepback.StepBackPostProcessors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Chat call wrapper for Step-Back prompt execution with normalization and gating.
 */
@RequiredArgsConstructor
public class StepBackCall extends ChatCallBase implements ChatCall {

    @Getter
    /**
     * Original user query.
     */
    private final String query;

    @Getter
    /**
     * Chat client builder used for Step-Back calls (separate conversation memory).
     */
    private final ChatClientBuilder chatClientBuilder;

    @Getter
    /**
     * Prompt messages for the Step-Back template.
     */
    private final MessageList messages;

    @Getter
    /**
     * Selector determining which messages are sent to the LLM.
     */
    private final MessageSelector messageSelector;

    /**
     * Applies Step-Back specific post-processing (normalization, clarification gating, reasoning-id actions).
     */
    @Override
    protected Map<String, Object> postProcess(Map<String, Object> rawResult) {
        return applyPostProcessors(rawResult, List.of(
                StepBackPostProcessors.normalize(),
                StepBackPostProcessors.syncClarification(),
                StepBackPostProcessors.limitQuestions(3),
                StepBackPostProcessors.reasoningIdAction(),
                PostProcessors.retainQuery(query)
        ));
    }
}
