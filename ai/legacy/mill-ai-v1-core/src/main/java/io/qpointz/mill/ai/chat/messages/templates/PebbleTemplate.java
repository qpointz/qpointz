package io.qpointz.mill.ai.chat.messages.templates;

import io.pebbletemplates.pebble.PebbleEngine;
import io.qpointz.mill.MillRuntimeException;
import io.qpointz.mill.ai.chat.messages.MessageTemplate;
import io.qpointz.mill.ai.chat.messages.MessageTemplateSource;
import lombok.val;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

public class PebbleTemplate implements MessageTemplate {

    private final MessageTemplateSource source;

    private final static PebbleEngine defaultEngine = new PebbleEngine.Builder()
            .cacheActive(true)
            .autoEscaping(false)
            .build();

    public PebbleTemplate(MessageTemplateSource source) {
        this.source = source;
    }

    @Override
    public String render(Map<String, Object> metadata) {
        val writer = new StringWriter();
        try {
            defaultEngine
                    .getLiteralTemplate(this.content())
                    .evaluate(writer, metadata);
            return writer
                    .toString();
        } catch (IOException e) {
            throw new MillRuntimeException(e);
        }
    }

    @Override
    public String content() {
        return source.content();
    }
}

