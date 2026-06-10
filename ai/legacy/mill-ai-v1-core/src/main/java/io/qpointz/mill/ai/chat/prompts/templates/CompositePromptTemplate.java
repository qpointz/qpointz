package io.qpointz.mill.ai.chat.prompts.templates;

import io.qpointz.mill.ai.chat.prompts.PromptTemplate;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@AllArgsConstructor
public class CompositePromptTemplate implements PromptTemplate {

    public record Item(PromptTemplate template, Map<String,Object> variables) {}

    private final List<Item> templates;

    @Override
    public String render(Map<String, Object> values) throws IOException {
        final Map<String, Object> initialValues = values != null
                ? values
                : Map.of();

        try (val stringWriter = new StringWriter()) {
            for (val kv: templates) {
                val template = kv.template();
                val templateValues = kv.variables();
                val combinedValues = new HashMap<>(initialValues);
                if (templateValues!=null && !templateValues.isEmpty()) {
                    combinedValues.putAll(templateValues);
                }
                stringWriter.append(template.render(combinedValues));
            }
            return stringWriter.toString();
        }
    }

    public static Item compositeItem(PromptTemplate template, Map<String, Object> values) {
        return new Item(template, values);
    }

    public static Item compositeItem(PromptTemplate template) {
        return new Item(template, Map.of());
    }

}
