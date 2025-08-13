package io.qpointz.mill.ai.chat.messages.templates.sources;

import io.qpointz.mill.MillRuntimeException;
import io.qpointz.mill.ai.chat.messages.MessageTemplateSource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.nio.charset.Charset;

public final class ResourceLoaderSource implements MessageTemplateSource {

    private final String content;

    public ResourceLoaderSource(String location, ResourceLoader resourceLoader) {
        try {
            this.content = resourceLoader
                    .getResource(location)
                    .getContentAsString(Charset.defaultCharset());
        } catch (IOException e) {
            throw new MillRuntimeException(e);
        }
    }

    @Override
    public String content() {
        return this.content;
    }
}