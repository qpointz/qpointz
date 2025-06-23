package io.qpointz.mill.ai.chat.prompts.templates;

import io.pebbletemplates.pebble.PebbleEngine;
import io.pebbletemplates.pebble.template.PebbleTemplate;
import io.qpointz.mill.ai.chat.prompts.PromptTemplate;
import io.qpointz.mill.ai.chat.prompts.PromptTemplateSources;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.val;



import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;


public abstract class PebblePromptTemplate implements PromptTemplate {

    @Getter(value = AccessLevel.PROTECTED, lazy = true)
    private final static PebbleEngine defaultEngine = new PebbleEngine.Builder()
            .cacheActive(true)
            .build();

    protected abstract PebbleTemplate getTemplate();

    protected static PebbleTemplate createTemplate(String name) {
        return PebblePromptTemplate
                .getDefaultEngine()
                .getTemplate(name);
    }

    public static PebblePromptTemplate create(String name) {
        val template = createTemplate(name);
        return new SimplePebblePromptTemplate(template);
    }

    public static PebbleTemplate createInlineTemplate(String content) {
        return PebblePromptTemplate
                .getDefaultEngine()
                .getLiteralTemplate(content);
    }

    public static PebbleTemplate createInlineTemplate(String location, ClassLoader classLoader) throws IOException {
        val sources = PromptTemplateSources.resourceSource(location, classLoader);
        return PebblePromptTemplate.createInlineTemplate(sources.content());
    }

    public static PromptTemplate createInline(String content) {
        return new SimplePebblePromptTemplate(createInlineTemplate(content));
    }

    protected abstract Map<String, Object> applyValues();

    @Override
    public String render(Map<String, Object> values) throws IOException {
        val actualParams = new HashMap<String, Object>();
        if (values != null && !values.isEmpty()) {
            actualParams.putAll(values);
        }

        val templateValues = applyValues();

        if (templateValues!=null && !templateValues.isEmpty()) {
            actualParams.putAll(applyValues());
        }

        try (val writer = new StringWriter()) {
            this.getTemplate().evaluate(writer, actualParams);
            return writer.toString();
        }
    }

}
