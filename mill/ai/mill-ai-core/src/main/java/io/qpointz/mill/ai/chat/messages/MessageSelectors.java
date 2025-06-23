package io.qpointz.mill.ai.chat.messages;

import org.springframework.ai.chat.messages.Message;

import java.util.List;

public class MessageSelectors {

    public static class SimpleSelector implements MessageSelector {
        @Override
        public List<Message> select(MessageList list) {
            return list.all();
        }
    }

    public final static MessageSelector SIMPLE = new SimpleSelector();



}
