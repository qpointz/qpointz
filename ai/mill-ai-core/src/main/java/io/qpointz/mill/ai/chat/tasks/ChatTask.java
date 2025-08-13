package io.qpointz.mill.ai.chat.tasks;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qpointz.mill.ai.chat.prompts.PromptTemplate;
import io.qpointz.mill.utils.JsonUtils;
import lombok.val;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import static io.qpointz.mill.ai.chat.prompts.PromptTemplateSources.resourceSource;
import static io.qpointz.mill.ai.chat.prompts.PromptTemplates.pebbleTemplate;

public abstract class ChatTask {

    protected abstract ChatClient getChatClient();

    protected abstract PromptTemplate getUserPromptTemplate() throws IOException;

    protected abstract String getUser();

    protected abstract UUID getChatId();

    private final PromptTemplate userQuestionTemplate;

    public ChatTask() {
        try {
            userQuestionTemplate = pebbleTemplate(
                    resourceSource("prompts/user-question.prompt", ChatTask.class.getClassLoader()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public class Response {

        private final String content;


        private Response(String content) {
            this.content = content;
        }

        public String content() {
            return this.content;
        }

        public <T> T entity(Class<T> valueType) {
            try {
                return JsonUtils.defaultJsonMapper().readValue(content, valueType);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        public Map<String, Object> asMap() {
            return entity(Map.class);
        }
        

    }

    private Response response = null;

    public Response invoke() throws IOException {
        synchronized (this) {
            if (this.response == null) {
                val user = userQuestionTemplate
                        .render(Map.of("userQuestion", this.getUser()));
                val content = this.getChatClient()
                        .prompt(getUserPromptTemplate().prompt())
                        .user(user)
                        .advisors(a-> a.param(ChatMemory.CONVERSATION_ID, this.getChatId().toString()))
                        .call()
                        .content();
                this.response = new Response(content);
            }
            return this.response;
        }
    }

    /*
    private <T> T callAs(Class<T> valueType) throws IOException {
        val respEntity = call()
                .call()
                .responseEntity(valueType);
        return respEntity
                .getEntity();
    }

    private Map<String, Object> callAsMap() throws IOException {
        return callAs(Map.class);
    }*/

}
