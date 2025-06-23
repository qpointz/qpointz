package io.qpointz.mill.ai.chat.messages.templates.sources;

import io.qpointz.mill.ai.chat.messages.MessageTemplateSource;

public final class InlineSource implements MessageTemplateSource {

    public final String content;

    public InlineSource(String content) {
        this.content = content;
    }

    @Override
    public String content() {
        return this.content;
    }
}
