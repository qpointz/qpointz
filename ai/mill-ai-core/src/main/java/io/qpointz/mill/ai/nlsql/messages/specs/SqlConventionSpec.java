package io.qpointz.mill.ai.nlsql.messages.specs;

import io.qpointz.mill.ai.chat.messages.MessageSpec;
import io.qpointz.mill.ai.chat.messages.MessageTemplate;
import org.springframework.ai.chat.messages.MessageType;

import java.util.Map;

public class SqlConventionSpec extends MessageSpec {
    @Override
    public MessageType getMessageType() {
        return null;
    }

    @Override
    public Map<String, Object> getMetadata() {
        return Map.of();
    }

    @Override
    public MessageTemplate getTemplate() {
        return null;
    }
}
