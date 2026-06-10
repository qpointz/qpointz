package io.qpointz.mill.ai.chat.messages.templates.sources;

import io.qpointz.mill.MillRuntimeException;
import io.qpointz.mill.ai.chat.messages.MessageTemplateSource;
import lombok.val;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public final class ResourceSource implements MessageTemplateSource {

    private final String content;

    public ResourceSource(String location, ClassLoader classLoader) {
        try (val in = classLoader.getResourceAsStream(location)) {
            val bytes = in.readAllBytes();
            this.content = new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new MillRuntimeException(e);
        }
    }

    @Override
    public String content() {
        return content;
    }
}
