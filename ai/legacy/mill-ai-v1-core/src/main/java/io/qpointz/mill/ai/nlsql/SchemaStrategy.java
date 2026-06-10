package io.qpointz.mill.ai.nlsql;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum SchemaStrategy {
    FULL_IN_SYSTEM_PROMPT,
    PARTIAL_RUNTIME_INJECTION,
    NONE;

    @JsonCreator
    public static SchemaStrategy fromString(String key) {
        return key == null ? null : SchemaStrategy.valueOf(key.toUpperCase());
    }
}
