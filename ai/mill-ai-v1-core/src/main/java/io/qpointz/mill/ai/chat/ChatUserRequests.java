package io.qpointz.mill.ai.chat;

import lombok.AllArgsConstructor;

import java.util.Optional;

public class ChatUserRequests {

    private ChatUserRequests() {
        //avoid instantiation
    }

    private record QueryRequest(String query, Optional<String> reasoningId) implements ChatUserRequest {
        public QueryRequest(String query) {
            this(query, Optional.empty());
        }
    }

    public static ChatUserRequest query(String query) {
        return new QueryRequest(query);
    }

    public static ChatUserRequest clarify(String query, String reasoningId) {
        return new QueryRequest(query, Optional.of(reasoningId));
    }

}
