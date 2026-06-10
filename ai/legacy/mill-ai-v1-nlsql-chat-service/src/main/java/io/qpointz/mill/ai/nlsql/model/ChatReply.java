package io.qpointz.mill.ai.nlsql.model;

import java.time.ZonedDateTime;
import java.util.Map;

/**
 * Represents a chat reply payload paired with the time it was produced.
 */
public record ChatReply(Map<String,Object> result, ZonedDateTime timestamp) {

    /**
     * Factory method capturing current time for a result payload.
     */
    public static ChatReply of(Map<String,Object> result) {
        return new ChatReply(result, ZonedDateTime.now());
    }
}
