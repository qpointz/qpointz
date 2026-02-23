package io.qpointz.mill.ai.nlsql;

import io.qpointz.mill.ai.chat.ChatUserRequest;
import io.qpointz.mill.ai.chat.messages.MessageSelector;
import io.qpointz.mill.ai.nlsql.models.SqlDialect;
import io.qpointz.mill.data.backend.dispatchers.DataOperationDispatcher;
import io.qpointz.mill.metadata.MetadataProvider;
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
                           MetadataProvider metadataProvider,
                           SqlDialect dialect,
                           DataOperationDispatcher dispatcher,
                           MessageSelector messageSelector,
                           ValueMapper valueMapper,
                           Reasoner reasoner,
                           ChatEventProducer eventProducer) {
        this.eventProducer = eventProducer;
        this.intentSpecs = new IntentSpecs(metadataProvider, dialect, chatBuilders , dispatcher, messageSelector, valueMapper, eventProducer);
        this.reasoner = reasoner;
    }

    public ReasoningReply reason(ChatUserRequest request) {
        eventProducer.beginProgressEvent("Thinking");
        val reply =  this.reasoner.reason(request);
        return reply;
    }

    public ChatReply query(ChatUserRequest request) {
        val reasonReply = this.reason(request);

        //intent not known
        if (reasonReply.result() == ReasoningReply.ReasoningResult.REASONED) {
            val intentCall = this.intentSpecs.getIntentCall(reasonReply.reasoningResponse());
            return ChatReply.reply(intentCall);
        }

        //has resolved intent already or clarification needed
        return reasonReply.reply();
    }



}
