package io.qpointz.mill.ai.nlsql;

import io.qpointz.mill.ai.chat.ChatUserRequest;
import io.qpointz.mill.ai.chat.messages.MessageSelector;
import io.qpointz.mill.ai.nlsql.models.SqlDialect;
import io.qpointz.mill.data.backend.dispatchers.DataOperationDispatcher;
import io.qpointz.mill.metadata.service.MetadataService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public class ChatApplication {

    @Getter
    private final IntentSpecs intentSpecs;

    private final Reasoner reasoner;

    private final ChatEventProducer eventProducer;

    public ChatApplication(CallSpecsChatClientBuilders chatBuilders,
                           MetadataService metadataService,
                           SqlDialect dialect,
                           DataOperationDispatcher dispatcher,
                           MessageSelector messageSelector,
                           ValueMapper valueMapper,
                           Reasoner reasoner,
                           ChatEventProducer eventProducer) {
        this.eventProducer = eventProducer;
        this.intentSpecs = new IntentSpecs(metadataService, dialect, chatBuilders, dispatcher, messageSelector, valueMapper, eventProducer);
        this.reasoner = reasoner;
    }

    public ReasoningReply reason(ChatUserRequest request) {
        eventProducer.beginProgressEvent("Thinking");
        val reply = this.reasoner.reason(request);
        return reply;
    }

    public ChatReply query(ChatUserRequest request) {
        val reasonReply = this.reason(request);

        if (reasonReply.result() == ReasoningReply.ReasoningResult.REASONED) {
            val intentCall = this.intentSpecs.getIntentCall(reasonReply.reasoningResponse());
            return ChatReply.reply(intentCall);
        }

        return reasonReply.reply();
    }
}
