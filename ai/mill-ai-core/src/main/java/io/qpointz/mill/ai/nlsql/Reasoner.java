package io.qpointz.mill.ai.nlsql;

import io.qpointz.mill.ai.chat.ChatUserRequest;

public interface Reasoner {
    ReasoningReply reason(ChatUserRequest request);
}
