package io.qpointz.mill.ai.nlsql;

import io.qpointz.mill.ai.nlsql.models.ReasoningResponse;

public record ReasoningReply(ReasoningResult result, ChatReply reply, String reasoningId) {

    public static ReasoningReply reasoned(ChatReply reply) {
        return new ReasoningReply(ReasoningResult.REASONED, reply, null);
    }

    public enum ReasoningResult {
        //reasoning completed and has intent ready to execution
        INTENT,

        //reasoning completed intent identified but not resolved
        REASONED,

        //reasoning need user clarification to continue reasoning
        CLARIFY
    }

    public ReasoningResponse reasoningResponse() {
        if (this.result()!=ReasoningResult.REASONED) {
            throw new IllegalStateException(String.format("Reasoning reply in '%s' state.", this.result.toString()));
        }

        return this.reply.as(ReasoningResponse.class);
    }

}
