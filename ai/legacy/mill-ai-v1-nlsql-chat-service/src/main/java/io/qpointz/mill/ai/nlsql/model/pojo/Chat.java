package io.qpointz.mill.ai.nlsql.model.pojo;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * DTO representing chat metadata exchanged via the API.
 */
public record Chat(
        UUID id,
        String name,
        Boolean isFavorite,
        ZonedDateTime created) {
    /**
     * Request payload to create a chat seeded with an initial message.
     */
    public record CreateChatRequest(String name) {}

    /**
     * Request payload to update chat name or favorite flag.
     */
    public record UpdateChatRequest(Optional<String> chatName, Optional<Boolean> isFavorite) {}

    /**
     * Request payload representing a user message (text plus optional structured content).
     */
    public record SendChatMessageRequest(String message, Map<String, Object> content) {}
}
