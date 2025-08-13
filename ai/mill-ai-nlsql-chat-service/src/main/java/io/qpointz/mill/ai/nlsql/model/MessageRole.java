package io.qpointz.mill.ai.nlsql.model;

public enum MessageRole {
    USER(1),
    CHAT(2),
    SYSTEM(3);

    public final int value;

    MessageRole(int value) {
        this.value = value;
    }
}
