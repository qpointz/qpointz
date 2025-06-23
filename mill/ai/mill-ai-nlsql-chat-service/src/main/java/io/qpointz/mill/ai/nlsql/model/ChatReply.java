package io.qpointz.mill.ai.nlsql.model;

import java.time.ZonedDateTime;
import java.util.Map;

public record ChatReply(Map<String,Object> result, ZonedDateTime timestamp) {

    public static ChatReply of(Map<String,Object> result) {
        return new ChatReply(result, ZonedDateTime.now());
    }
}
