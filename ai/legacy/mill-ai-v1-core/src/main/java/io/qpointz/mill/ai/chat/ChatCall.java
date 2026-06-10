package io.qpointz.mill.ai.chat;

import io.qpointz.mill.utils.JsonUtils;
import lombok.val;

import java.util.Map;

public interface ChatCall {

    @Deprecated
    default Map<String,Object> asMap() {
        return this.call().contentAsMap();
    }

    @Deprecated
    default <T> T as(Class<T> valueType) {
        return this.call().contentAs(valueType);
    }

    ChatCallResponse call();



}
