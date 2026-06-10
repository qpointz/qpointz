package io.qpointz.mill.ai.chat.messages;

import io.pebbletemplates.pebble.PebbleEngine;
import io.qpointz.mill.MillRuntimeException;
import io.qpointz.mill.ai.chat.messages.templates.PebbleTemplate;
import io.qpointz.mill.ai.chat.messages.templates.StaticTemplate;
import lombok.val;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import static io.qpointz.mill.ai.chat.messages.MessageTemplateSources.inline;
import static io.qpointz.mill.ai.chat.messages.MessageTemplateSources.resource;

public class MessageTemplates {


    public static MessageTemplate staticTemplate(MessageTemplateSource source) {
        return new StaticTemplate(source);
    }

    @Deprecated()
    public static MessageTemplate staticTemplate(String content) {
        return text(content);
    }

    public static MessageTemplate text(String content) {
        return new StaticTemplate(inline(content));
    }

    public static MessageTemplate staticTemplate(String location, Class valueType) {
        return new StaticTemplate(resource(location, valueType.getClassLoader()));
    }

    public static MessageTemplate staticTemplate(String location, ResourceLoader resourceLoader) {
        return new StaticTemplate(resource(location, resourceLoader));
    }

    public static MessageTemplate pebbleTemplate(MessageTemplateSource source) {
        return new PebbleTemplate(source);
    }

    public static MessageTemplate pebbleTemplate(String content) {
        return new PebbleTemplate(inline(content));
    }

    public static MessageTemplate pebbleTemplate(String location, Class valueType) {
        return new PebbleTemplate(resource(location, valueType.getClassLoader()));
    }

    public static MessageTemplate pebbleTemplate(String location, ResourceLoader resourceLoader) {
        return new PebbleTemplate(resource(location, resourceLoader));
    }
}
