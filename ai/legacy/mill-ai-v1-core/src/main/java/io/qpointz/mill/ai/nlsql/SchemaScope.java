package io.qpointz.mill.ai.nlsql;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum SchemaScope {
    FULL,
    PARTIAL,
    NONE;

    @JsonCreator
    public static SchemaScope fromString(String key) {
        return key == null ? null : SchemaScope.valueOf(key.toUpperCase());
    }
}
