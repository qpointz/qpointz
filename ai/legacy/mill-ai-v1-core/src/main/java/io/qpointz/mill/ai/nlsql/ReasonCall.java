package io.qpointz.mill.ai.nlsql;

import io.qpointz.mill.ai.chat.ChatCall;
import io.qpointz.mill.ai.chat.ChatClientBuilder;
import io.qpointz.mill.ai.chat.messages.MessageList;
import io.qpointz.mill.ai.chat.messages.MessageSelector;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

import static io.qpointz.mill.ai.nlsql.PostProcessors.checkIntentPresent;
import static io.qpointz.mill.ai.nlsql.PostProcessors.retainQuery;

@AllArgsConstructor
public class ReasonCall extends ChatCallBase {

    @Getter
    private final String query;

    @Getter
    private ChatClientBuilder chatClientBuilder;

    @Getter
    private MessageList messages;

    @Getter
    private MessageSelector messageSelector;

    @Override
    protected Map<String, Object> postProcess(Map<String, Object> rawResult) {
        return applyPostProcessors(rawResult,
                List.of(checkIntentPresent(), retainQuery(query)));
    }
}
