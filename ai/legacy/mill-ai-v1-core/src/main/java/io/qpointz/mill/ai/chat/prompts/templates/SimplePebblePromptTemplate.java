package io.qpointz.mill.ai.chat.prompts.templates;

import io.pebbletemplates.pebble.template.PebbleTemplate;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.IOException;
import java.util.Map;

@AllArgsConstructor
public final class SimplePebblePromptTemplate extends PebblePromptTemplate {

    @Getter
    private final PebbleTemplate template;


    @Override
    public Map<String, Object>  applyValues(){
        return Map.of();
    }
}
