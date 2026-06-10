package io.qpointz.mill.ai.chat.messages.templates;

import io.qpointz.mill.ai.chat.messages.MessageTemplate;
import io.qpointz.mill.ai.chat.messages.MessageTemplateSource;

import java.util.Map;

public class StaticTemplate implements MessageTemplate {
    private final MessageTemplateSource source;

    public StaticTemplate(MessageTemplateSource source) {
        this.source = source;
    }

    @Override
    public String render(Map<String, Object> metadata) {
        return source.content();
    }

    @Override
    public String content() {
        return source.content();
    }
}
