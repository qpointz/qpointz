package io.qpointz.mill.ai.nlsql.model.pojo;

import io.qpointz.mill.ai.nlsql.model.MessageRole;

import java.util.Map;
import java.util.UUID;

public record ChatMessage(
        UUID id,
        String message,
        MessageRole role,
        Map<String,Object> content
) {
}
