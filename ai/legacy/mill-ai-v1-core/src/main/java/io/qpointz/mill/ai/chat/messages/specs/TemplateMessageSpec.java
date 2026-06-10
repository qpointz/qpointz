package io.qpointz.mill.ai.chat.messages.specs;

import io.qpointz.mill.ai.chat.messages.MessageSpec;
import io.qpointz.mill.ai.chat.messages.MessageTemplate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.ai.chat.messages.MessageType;

import java.util.Map;

@AllArgsConstructor
public class TemplateMessageSpec extends MessageSpec {

    public TemplateMessageSpec(MessageType messageType, MessageTemplate template) {
        this(messageType, template, Map.of());
    }

    public TemplateMessageSpec(MessageType messageType, MessageTemplate template, Map<String, Object> metadata) {
        this.messageType = messageType;
        this.template = template;
        this.metadata = metadata;
    }

    @Getter
    private final MessageType messageType;

    @Getter
    private final Map<String, Object> metadata;

    @Getter
    private final MessageTemplate template;
}
