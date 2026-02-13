package io.qpointz.mill.ai.nlsql.model;

/**
 * Roles associated with chat messages.
 */
public enum MessageRole {
    USER(1),
    CHAT(2),
    SYSTEM(3);

    public final int value;

    /**
     * @param value ordinal persisted to storage or clients
     */
    MessageRole(int value) {
        this.value = value;
    }
}
