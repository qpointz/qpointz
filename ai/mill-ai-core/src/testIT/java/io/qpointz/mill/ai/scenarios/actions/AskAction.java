package io.qpointz.mill.ai.scenarios.actions;

import io.qpointz.mill.ai.chat.ChatCallResponse;
import io.qpointz.mill.ai.chat.ChatUserRequests;
import io.qpointz.mill.ai.scenarios.ChatAppActionExecutor;
import io.qpointz.mill.ai.scenarios.ChatAppScenarioContext;
import io.qpointz.mill.ai.testing.scenario.Action;
import io.qpointz.mill.ai.testing.scenario.ActionOutcome;
import lombok.val;

import java.util.Map;

public class AskAction implements ChatAppActionExecutor {

    @Override
    public ActionOutcome executeAction(ChatAppScenarioContext context, Action action) throws Exception {
        val value = action.getParamAs("value", String.class).get();
        val reply = context.getChatApplication().query(ChatUserRequests.query(value));
        val callResponse = reply.getChatCall();
        val content = reply.asMap();
        val metrics = getCallMetrics(callResponse.call());
        return ActionOutcome.of(ActionOutcome.OutcomeStatus.PASS, content, metrics);
    }

    public static Map<String,Object> getCallMetrics(ChatCallResponse resp) {
        return resp.getResponse()
                .map(r-> Map.<String,Object>of(
                        "llm.model", r.getMetadata().getModel(),
                        "llm.usage.completion-tokens", r.getMetadata().getUsage().getCompletionTokens(),
                        "llm.usage.prompt-tokens", r.getMetadata().getUsage().getPromptTokens(),
                        "llm.usage.total-tokens", r.getMetadata().getUsage().getTotalTokens()
                ))
                .orElse(Map.of());
    }

}
