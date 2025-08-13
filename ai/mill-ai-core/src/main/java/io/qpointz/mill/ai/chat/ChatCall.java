package io.qpointz.mill.ai.chat;

import io.qpointz.mill.utils.JsonUtils;
import lombok.val;

import java.util.Map;

public interface ChatCall {

    Map<String,Object> asMap();

    default <T> T as(Class<T> valueType) {
        val map = this.asMap();
        return JsonUtils
                .defaultJsonMapper()
                .convertValue(map, valueType);
    }

}
