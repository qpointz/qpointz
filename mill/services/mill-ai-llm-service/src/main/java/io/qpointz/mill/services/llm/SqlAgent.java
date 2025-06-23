package io.qpointz.mill.services.llm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qpointz.mill.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.Map;

@Slf4j
public class SqlAgent {

    private final ChatClient chatClient;
    private final PromptBuilder promptBuilder;

    public SqlAgent(ChatClient chatClient, PromptBuilder promptBuilder) {
        //val prompt = new Prompt(promptBuilder.prompt());
        //chatClient.prompt(prompt);
       this.chatClient = chatClient;
       this.promptBuilder = promptBuilder;
       val resp = this.chatClient.prompt(promptBuilder.prompt()).call().content();
       log.debug(resp.trim());
    }

    public String query(String query) {
        return this.chatClient
                .prompt(promptBuilder.prompt())
                .user(query)
                .call()
                .content()
                .trim();
    }


    public JsonNode queryAsJson(String query) throws JsonProcessingException {
        val mayBeJson = this.query(query);
        return JsonUtils.defaultJsonMapper().readTree(mayBeJson);
    }

    public Map<String, Object> queryAsMap(String query) throws JsonProcessingException {
        val mayBeJson = this.query(query);
        return JsonUtils.defaultJsonMapper().readValue(mayBeJson, Map.class);
    }
    
}
