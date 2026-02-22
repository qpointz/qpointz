package io.qpointz.mill.ai.nlsql.reasoners;

import io.qpointz.mill.ai.chat.ChatCall;
import io.qpointz.mill.ai.chat.ChatUserRequest;
import io.qpointz.mill.ai.chat.messages.MessageSelector;
import io.qpointz.mill.ai.nlsql.*;
import io.qpointz.mill.data.backend.metadata.MetadataProvider;

public class DefaultReasoner implements Reasoner {

    private CallSpecsChatClientBuilders chatBuilders;
    private MetadataProvider metadaProvider;
    private MessageSelector messageSelector;

    public DefaultReasoner(CallSpecsChatClientBuilders chatBuilders,
                                MetadataProvider metadataProvider,
                                MessageSelector messageSelector) {
        this.chatBuilders = chatBuilders;
        this.metadaProvider = metadataProvider;
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
                MessageSpecs.reason(query, this.metadaProvider),
                this.messageSelector);
    }

}
