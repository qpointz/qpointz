package io.qpointz.mill.ai.chat.messages;

import io.qpointz.mill.MillRuntimeException;
import lombok.*;
import org.apache.commons.codec.digest.MurmurHash3;
import org.springframework.ai.chat.messages.*;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Stream;

@AllArgsConstructor
@Builder
public class MessageList {

    @Getter
    @Singular
    private final List<MessageSpec> specs;

    private List<Message> asMessageList(Stream<MessageSpec> specs) {
        return specs
                .<Message>map(k->
                    switch (k.getMessageType()) {
                        case USER -> new UserMessage(k.getText());
                        case SYSTEM -> new SystemMessage(k.getText());
                        case ASSISTANT -> new AssistantMessage(k.getText());
                        default -> throw new MillRuntimeException(String.format("Message type %s not supported", k.getMessageType()));
                    })
                .toList();
    }

    public List<Message> all() {
        return asMessageList(specs.stream());
    }


    public List<Message> nonSystem() {
        return asMessageList(specs.stream()
                .filter(k-> k.getMessageType()!= MessageType.SYSTEM));
    }

    public List<Message> system() {
        return asMessageList(specs.stream()
                .filter(k-> k.getMessageType() == MessageType.SYSTEM));
    }

    public int systemHash() {
        val sb = new StringBuilder();

        specs.stream()
                .filter(k-> k.getMessageType() == MessageType.SYSTEM)
                .forEach(k-> sb.append(
                    k.getTemplate().content()));

        val content = sb.toString()
                .getBytes(StandardCharsets.UTF_8);

        return MurmurHash3.hash32x86(content);
    }



}
