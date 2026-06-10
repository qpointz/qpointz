package io.qpointz.mill.ai.nlsql.model.pojo;

import io.qpointz.mill.ai.nlsql.model.MessageRole;

import java.util.Map;
import java.util.UUID;

/**
 * DTO representing a single chat message for API responses.
 */
public record ChatMessage(
        UUID id,
        String message,
        MessageRole role,
        Map<String,Object> content
) {
    /**
     * DTO representing a chat message with role and optional structured content.
     */
}
