package io.qpointz.mill.ai.scenarios;

import io.qpointz.mill.ai.chat.ChatUserRequests;
import io.qpointz.mill.ai.scenarios.actions.AskAction;
import io.qpointz.mill.ai.scenarios.actions.ReplyAction;
import io.qpointz.mill.ai.scenarios.actions.VerifyAction;
import io.qpointz.mill.test.scenario.Action;
import io.qpointz.mill.test.scenario.ActionOutcome;
import io.qpointz.mill.test.scenario.ActionResult;
import io.qpointz.mill.test.scenario.ScenarioRunner;
import lombok.val;

public class ChatAppScenarioRunner extends ScenarioRunner<ChatAppScenarioContext, ActionResult> {

    /**
     * Creates a new scenario runner with the given context.
     *
     * @param context the scenario context to use
     * @throws IllegalArgumentException if context is null
     */
    protected ChatAppScenarioRunner(ChatAppScenarioContext context) {
        super(context);
    }

    @Override
    protected ActionOutcome executeAction(ChatAppScenarioContext context, Action action) throws Exception {
        return getExecutor(action).executeAction(context, action);
    }

    protected ChatAppActionExecutor getExecutor(Action action) throws Exception {
        return switch (action.key()) {
            case "ask" -> new AskAction();
            case "verify" -> new VerifyAction();
            case "reply" -> new ReplyAction();
            default -> throw new Exception("Unknwon action:"+action.key());
        };
    }

}
