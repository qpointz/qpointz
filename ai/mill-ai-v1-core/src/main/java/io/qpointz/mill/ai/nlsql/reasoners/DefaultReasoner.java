package io.qpointz.mill.ai.nlsql.reasoners;

import io.qpointz.mill.ai.chat.ChatCall;
import io.qpointz.mill.ai.chat.ChatUserRequest;
import io.qpointz.mill.ai.chat.messages.MessageSelector;
import io.qpointz.mill.ai.nlsql.*;
import io.qpointz.mill.ai.nlsql.metadata.SchemaMessageMetadataPorts;

public class DefaultReasoner implements Reasoner {

    private CallSpecsChatClientBuilders chatBuilders;
    private SchemaMessageMetadataPorts schemaPorts;
    private MessageSelector messageSelector;

    public DefaultReasoner(CallSpecsChatClientBuilders chatBuilders,
                                SchemaMessageMetadataPorts schemaPorts,
                                MessageSelector messageSelector) {
        this.chatBuilders = chatBuilders;
        this.schemaPorts = schemaPorts;
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
                MessageSpecs.reason(query, this.schemaPorts),
                this.messageSelector);
    }
}
