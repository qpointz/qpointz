package io.qpointz.mill.ai.chat.messages;

import org.springframework.ai.chat.messages.Message;

import java.util.List;

public interface MessageSelector {

    List<Message> select(MessageList list);

}
