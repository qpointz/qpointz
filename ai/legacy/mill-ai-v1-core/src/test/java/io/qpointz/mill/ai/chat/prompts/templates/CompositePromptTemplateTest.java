package io.qpointz.mill.ai.chat.prompts.templates;

import lombok.val;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static io.qpointz.mill.ai.chat.prompts.PromptTemplates.staticTemplate;
import static io.qpointz.mill.ai.chat.prompts.templates.CompositePromptTemplate.compositeItem;
import static org.junit.jupiter.api.Assertions.*;

class CompositePromptTemplateTest {

    @Test
    void trivial() throws IOException {
        val composite = new CompositePromptTemplate(List.of(
                compositeItem(staticTemplate("Hello")),
                compositeItem(staticTemplate(" World!!!"))));
        val result = composite.render();
        assertEquals("Hello World!!!", result);
    }

}