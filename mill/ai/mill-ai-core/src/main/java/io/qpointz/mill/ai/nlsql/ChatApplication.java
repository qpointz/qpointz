package io.qpointz.mill.ai.nlsql;

import io.qpointz.mill.ai.chat.ChatCall;
import io.qpointz.mill.ai.chat.messages.MessageSelector;
import io.qpointz.mill.ai.nlsql.models.ReasoningResponse;
import io.qpointz.mill.services.dispatchers.DataOperationDispatcher;
import io.qpointz.mill.services.metadata.MetadataProvider;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public class ChatApplication {

    @Getter
    private final MetadataProvider metadataProvider;

    @Getter
    private final DataOperationDispatcher dispatcher;

    @Getter
    private final IntentSpecs intentSpecs;

    public ChatApplication(CallSpecsChatClientBuilders chatBuilders, MetadataProvider metadataProvider, DataOperationDispatcher dispatcher, MessageSelector messageSelector) {
        this.metadataProvider = metadataProvider;
        this.dispatcher = dispatcher;
        this.intentSpecs = new IntentSpecs(metadataProvider, chatBuilders , dispatcher, messageSelector);
    }

    public ChatCall reason(String query) {
        return this.getIntentSpecs()
                .reasonCall(query);
    }

    public ChatCall query(String query) {
        val reasoningResponse =  this.reason(query)
                .as(ReasoningResponse.class);
        return getIntentCall(reasoningResponse);
    }

    private ChatCall getIntentCall(ReasoningResponse reasoningResponse) {
        log.info("Resolving intent '{}'", reasoningResponse.intent());
        log.info("Reasoning response:'{}'", reasoningResponse);
        val intent = reasoningResponse
                .intent()
                .toLowerCase();
        return this.intentSpecs
                .getIntent(intent)
                .getCall(reasoningResponse);
    }

}
