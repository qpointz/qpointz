package io.qpointz.mill.ai.nlsql.model.pojo;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public record Chat(
        UUID id,
        String name,
        Boolean isFavorite,
        ZonedDateTime created) {
    public record CreateChatRequest(String name) {}
    public record UpdateChatRequest(Optional<String> chatName, Optional<Boolean> isFavorite) {}
    public record SendChatMessageRequest(String message, Map<String, Object> content) {}
}

