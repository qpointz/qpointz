package io.qpointz.mill.ai.nlsql;

import io.qpointz.mill.ai.chat.ChatCall;
import io.qpointz.mill.utils.JsonUtils;
import lombok.AllArgsConstructor;
import lombok.val;

import java.util.Map;

public abstract class ChatReply {

    public abstract Map<String,Object> asMap();

    public <T> T as(Class<T> valueType) {
        val map = this.asMap();
        return JsonUtils
                .defaultJsonMapper()
                .convertValue(map, valueType);
    }

    @AllArgsConstructor
    private static class ChatCallReply extends ChatReply {
        private final ChatCall chatCall;

        @Override
        public Map<String, Object> asMap() {
            return this.chatCall.asMap();
        }
    }

    public static ChatReply reply(ChatCall chatCall) {
        return new ChatCallReply(chatCall);
    }

}
