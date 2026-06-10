package io.qpointz.mill.ai.chat.messages;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;

import java.util.Map;

@AllArgsConstructor
public abstract class MessageSpec implements Message {

    public abstract MessageType getMessageType();

    public abstract Map<String, Object> getMetadata();

    public abstract MessageTemplate getTemplate();

    @Override
    public String getText() {
        return this.getTemplate().render(this.getMetadata());
    }

}
