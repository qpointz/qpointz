package io.qpointz.mill.ai.scenarios.actions;

import io.qpointz.mill.ai.chat.ChatUserRequests;
import io.qpointz.mill.ai.scenarios.ChatAppActionExecutor;
import io.qpointz.mill.ai.scenarios.ChatAppScenarioContext;
import io.qpointz.mill.test.scenario.Action;
import io.qpointz.mill.test.scenario.ActionOutcome;
import lombok.val;

import java.util.Map;
import java.util.Optional;

public class ReplyAction implements ChatAppActionExecutor {
    @Override
    public ActionOutcome executeAction(ChatAppScenarioContext context, Action action) throws Exception {
        val msgIdx = action.getParamAs("message", Integer.class)
                .orElse(0);

        val reasoning = context.getLastResult(msgIdx)
                .outcome()
                .getDataAs(Map.class)
                .orElse(Map.of())
                .getOrDefault("reasoning-id", "")
                .toString();

        val value = action.getParamAs("with", String.class).get();
        val reply = context.getChatApplication().query(ChatUserRequests.clarify(value,reasoning));
        val callResponse = reply.getChatCall();
        val content = reply.asMap();
        val metrics = AskAction.getCallMetrics(callResponse.call());
        return ActionOutcome.of(ActionOutcome.OutcomeStatus.PASS, content,metrics);
    }
}
