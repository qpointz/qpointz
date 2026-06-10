package io.qpointz.mill.ai.chat.prompts.templates;

import io.qpointz.mill.ai.chat.prompts.PromptTemplate;
import io.qpointz.mill.ai.chat.prompts.PromptTemplateSource;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Map;

@Slf4j
@AllArgsConstructor
public final class StaticPromptTemplate implements PromptTemplate {

    private final PromptTemplateSource source;

    @Override
    public String render(Map<String, Object> values) throws IOException {
        log.warn("Static template ignores values");
        return this.render();
    }

    @Override
    public String render() throws IOException {
        return source.content();
    }
}
