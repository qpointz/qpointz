package io.qpointz.mill.ai.chat.prompts;

import io.qpointz.mill.ai.chat.prompts.sources.ResourceTemplateSource;
import io.qpointz.mill.ai.chat.prompts.sources.StringTemplateSource;
import lombok.val;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.yaml.snakeyaml.reader.StreamReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

public final class PromptTemplateSources {

    public static PromptTemplateSource resourceSource(Resource resource) {
        return new ResourceTemplateSource(resource);
    }

    public static PromptTemplateSource resourceSource(String location, ResourceLoader resourceLoader) {
        return resourceSource(resourceLoader.getResource(location));
    }

    public static PromptTemplateSource resourceSource(String location, ClassLoader classLoader) throws IOException {
        try (val in = classLoader.getResourceAsStream(location)) {
            val bytes = in.readAllBytes();
            val content = new String(bytes, StandardCharsets.UTF_8);
            return new StringTemplateSource(content);
        }
    }

    public static PromptTemplateSource string(String content) {
        return new StringTemplateSource(content);
    }
}
