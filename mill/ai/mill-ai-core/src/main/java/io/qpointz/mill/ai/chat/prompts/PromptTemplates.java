package io.qpointz.mill.ai.chat.prompts;

import io.qpointz.mill.ai.chat.prompts.templates.PebblePromptTemplate;
import io.qpointz.mill.ai.chat.prompts.templates.StaticPromptTemplate;

import java.io.IOException;

public class PromptTemplates {


    public static PromptTemplate staticTemplate(PromptTemplateSource source) {
        return new StaticPromptTemplate(source);
    }

    public static PromptTemplate staticTemplate(String location, ClassLoader classLoader) throws IOException {
        return new StaticPromptTemplate(PromptTemplateSources.resourceSource(location, classLoader));
    }

    public static PromptTemplate staticTemplate(String content) {
        return staticTemplate(PromptTemplateSources.string(content));
    }

    public static PromptTemplate pebbleTemplate(String name) {
        return PebblePromptTemplate.create(name);
    }

    public static PromptTemplate pebbleTemplate(PromptTemplateSource source) throws IOException {
        return PebblePromptTemplate.createInline(source.content());
    }

    public static PromptTemplate pebbleInlineTemplate(String content) {
        return PebblePromptTemplate.createInline(content);
    }



}
