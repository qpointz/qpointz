package io.qpointz.mill.ai.nlsql.reasoners;

import io.qpointz.mill.ai.chat.ChatCall;
import io.qpointz.mill.ai.chat.ChatUserRequest;
import io.qpointz.mill.ai.chat.messages.MessageSelector;
import io.qpointz.mill.ai.nlsql.*;
import io.qpointz.mill.metadata.service.MetadataService;

public class DefaultReasoner implements Reasoner {

    private CallSpecsChatClientBuilders chatBuilders;
    private MetadataService metadataService;
    private MessageSelector messageSelector;

    public DefaultReasoner(CallSpecsChatClientBuilders chatBuilders,
                                MetadataService metadataService,
                                MessageSelector messageSelector) {
        this.chatBuilders = chatBuilders;
        this.metadataService = metadataService;
        this.messageSelector = messageSelector;
    }

    @Override
    public ReasoningReply reason(ChatUserRequest request) {
        return ReasoningReply
                .reasoned(ChatReply.reply(reasonCall(request.query())));
    }

    private ChatCall reasonCall(String query) {
        return new ReasonCall(
                query,
                this.chatBuilders.reasoningChat(),
                MessageSpecs.reason(query, this.metadataService),
                this.messageSelector);
    }
}
