package io.qpointz.mill.ai.chat.messages;

import io.qpointz.mill.ai.chat.messages.templates.sources.InlineSource;
import io.qpointz.mill.ai.chat.messages.templates.sources.ResourceLoaderSource;
import io.qpointz.mill.ai.chat.messages.templates.sources.ResourceSource;
import org.springframework.core.io.ResourceLoader;

public class MessageTemplateSources {

    public static InlineSource inline(String content) {
        return new InlineSource(content);
    }

    public static ResourceSource resource(String location, ClassLoader classLoader) {
        return new ResourceSource(location, classLoader);
    }

    public static ResourceLoaderSource resource(String location, ResourceLoader resourceLoader) {
        return new ResourceLoaderSource(location, resourceLoader);
    }

}
