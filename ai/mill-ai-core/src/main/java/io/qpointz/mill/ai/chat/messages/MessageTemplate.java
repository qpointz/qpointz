package io.qpointz.mill.ai.chat.messages;

import java.util.Map;

public interface MessageTemplate {
    String render(Map<String,Object> metadata);
    String content();
}
