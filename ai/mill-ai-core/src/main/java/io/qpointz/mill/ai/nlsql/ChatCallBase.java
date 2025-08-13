package io.qpointz.mill.ai.nlsql;

import io.qpointz.mill.ai.chat.ChatCall;
import io.qpointz.mill.ai.chat.ChatCallPostProcessor;
import io.qpointz.mill.ai.chat.ChatClientBuilder;
import io.qpointz.mill.ai.chat.messages.MessageList;
import io.qpointz.mill.ai.chat.messages.MessageSelector;
import io.qpointz.mill.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.List;
import java.util.Map;

@Slf4j
public abstract class ChatCallBase implements ChatCall {

    protected abstract ChatClientBuilder getChatClientBuilder();

    protected abstract MessageList getMessages();

    protected abstract MessageSelector getMessageSelector();

    public List<Message> getSelectedMessages() {
        return this.getMessageSelector().select(this.getMessages());
    }

    @Override
    public Map<String,Object> asMap() {
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
        return postprocessed;
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
