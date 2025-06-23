package io.qpointz.mill.ai.chat;

import io.qpointz.mill.ai.chat.messages.HashUtils;
import io.qpointz.mill.ai.chat.messages.MessageList;
import io.qpointz.mill.ai.chat.messages.MessageSelector;
import io.qpointz.mill.ai.chat.messages.MessageSelectors;
import io.qpointz.mill.utils.JsonUtils;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.List;
import java.util.Map;
import java.util.Set;

@AllArgsConstructor
@Builder
@Slf4j
public class ChatCallSpec {

    @Getter
    private final ChatClientBuilder chatClientBuilder;

    @Getter
    private final MessageList messages;

    @Getter
    @Builder.Default
    private final MessageSelector messageSelector = MessageSelectors.SIMPLE;

    @Getter
    @Builder.Default
    private final List<ChatCallPostProcessor> postprocessors = List.of();

    @Getter
    @Builder.Default
    private final Set<Integer> promptHashes = Set.of();

    public List<Message> getSelectedMessages() {
        return this.getMessageSelector().select(this.getMessages());
    }

    public ChatCall call() {
        return new ChatCallImpl(this);
    }

    @AllArgsConstructor
    private class ChatCallImpl implements ChatCall {

        @Getter
        private final ChatCallSpec callSpec;

        private List<Message> preSelectMessages(List<Message> selectedMessages) {
            val hashes = this.getCallSpec().getPromptHashes();

            val evicted = selectedMessages.stream()
                    .filter(k-> ! hashes.contains(HashUtils.digest(k.getText())))
                    .toList();

            val dif = selectedMessages.size() - evicted.size();
            if (dif > 0) {
                log.info("Evicted {} prompts out of {}", dif, selectedMessages.size());
            }
            return evicted;
        }

        private ChatClient.CallResponseSpec call() {
            val client = this.callSpec.chatClientBuilder.build();
            val prompt = new Prompt(preSelectMessages(this.callSpec.getSelectedMessages()));
            return  client
                    .prompt(prompt)
                    .call();
        }

        public Map<String,Object> asMap() {
            val response = this.call();
            val map = response.entity(Map.class);
            val type = JsonUtils.defaultJsonMapper()
                    .getTypeFactory()
                    .constructParametricType(Map.class, String.class, Object.class);
            Map<String, Object> rawResult = JsonUtils.defaultJsonMapper()
                    .convertValue(map, type);
            log.debug("Chat response [Raw]:{}", rawResult);
            val postprocessed =  postprocess(rawResult);
            log.debug("Chat response [Post processed]:{}", postprocessed);
            return postprocessed;
        }

        private Map<String, Object> postprocess(Map<String, Object> rawResult) {
            return this.callSpec.getPostprocessors().stream()
                    .reduce(
                            rawResult,
                            (res, p) -> p.process(res),
                            (r1, r2) -> r2
                    );
        }

    }

}
