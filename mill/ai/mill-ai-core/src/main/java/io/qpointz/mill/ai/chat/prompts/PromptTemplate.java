package io.qpointz.mill.ai.chat.prompts;

import org.springframework.ai.chat.prompt.Prompt;

import java.io.IOException;
import java.util.Map;

public interface PromptTemplate {

    String render(Map<String,Object> values) throws IOException;

    default String render() throws IOException {
        return this.render(Map.of());
    }

    default Prompt prompt(Map<String, Object> values) throws IOException {
        return new Prompt(this.render(values));
    }

    default Prompt prompt() throws IOException {
        return new Prompt(this.render());
    }

}
