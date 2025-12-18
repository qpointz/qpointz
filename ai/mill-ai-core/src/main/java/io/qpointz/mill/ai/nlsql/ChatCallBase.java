package io.qpointz.mill.ai.nlsql;

import io.qpointz.mill.ai.chat.ChatCall;
import io.qpointz.mill.ai.chat.ChatCallPostProcessor;
import io.qpointz.mill.ai.chat.ChatCallResponse;
import io.qpointz.mill.ai.chat.ChatClientBuilder;
import io.qpointz.mill.ai.chat.messages.MessageList;
import io.qpointz.mill.ai.chat.messages.MessageSelector;
import io.qpointz.mill.utils.JsonUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
public abstract class ChatCallBase implements ChatCall {

    protected abstract ChatClientBuilder getChatClientBuilder();

    protected abstract MessageList getMessages();

    protected abstract MessageSelector getMessageSelector();

    public List<Message> getSelectedMessages() {
        return this.getMessageSelector().select(this.getMessages());
    }

    @AllArgsConstructor
    class ChatCallBaseResponse implements ChatCallResponse {

        @Getter
        private Map<String,Object> content;

        @Getter
        private Optional<ChatResponse> response;

        @Override
        public Map<String, Object> contentAsMap() {
            return content;
        }
    }


    @Override
    public ChatCallResponse call() {
        val prompt = new Prompt(this.getSelectedMessages());

        val client = this.getChatClientBuilder().build();

        val response =  client
                .prompt(prompt)
                .call();

        val map = response.entity(Map.class);

        val type = JsonUtils.defaultJsonMapper()
                .getTypeFactory()
                .constructParametricType(Map.class, String.class, Object.class);

        Map<String, Object> rawResult = JsonUtils.defaultJsonMapper()
                .convertValue(map, type);

        log.debug("Chat response [Raw]:{}", rawResult);
        val postprocessed = postProcess(rawResult);
        log.debug("Chat response [Post processed]:{}", postprocessed);

        Optional<ChatResponse> chatResponse = Optional.empty();
        try {
            chatResponse = Optional.of(response.chatResponse());
        } catch (Exception ex) {
            log.warn("Failed to get chat response:{}", ex.getMessage());
        }

        return new ChatCallBaseResponse(postprocessed, chatResponse);
    }

    protected Map<String, Object> postProcess(Map<String, Object> rawResult) {
        return rawResult;
    }

    public static Map<String,Object> applyPostProcessors(Map<String,Object> rawResult, List<ChatCallPostProcessor> postProcessors) {
        return postProcessors.stream()
                .reduce(
                        rawResult,
                        (res, p) -> p.process(res),
                        (r1, r2) -> r2
                );
    }

}
