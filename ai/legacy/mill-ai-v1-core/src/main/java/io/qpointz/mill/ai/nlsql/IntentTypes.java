package io.qpointz.mill.ai.nlsql;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum IntentTypes {
    GET_DATA,
    GET_CHART,
    EXPLAIN,
    DO_CONVERSATION,
    UNSUPPORTED;

    @JsonCreator
    public static IntentTypes fromString(String key) {
        return key == null
                ? null
                : IntentTypes.valueOf(key.toUpperCase());
    }
}