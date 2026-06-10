package io.qpointz.mill.ai.chat;

import io.qpointz.mill.utils.JsonUtils;
import lombok.val;
import org.springframework.ai.chat.model.ChatResponse;

import java.util.Map;
import java.util.Optional;

public interface ChatCallResponse {

    Optional<ChatResponse> getResponse();

    Map<String,Object> contentAsMap();

    default <T> T contentAs(Class<T> valueType) {
        val map = this.contentAsMap();
        return JsonUtils
                .defaultJsonMapper()
                .convertValue(map, valueType);
    }

}
