-- AI v3 chat metadata table.
-- Persists chat resource identity, ownership, naming, favorites, and context binding.
-- Separate from transcript (ai_conversation) and LLM memory (chat_memory).
-- Delete semantics: hard-delete; cascade cleanup is handled at the service layer.

CREATE TABLE ai_chat_metadata (
    chat_id             VARCHAR(255) PRIMARY KEY,
    user_id             VARCHAR(255) NOT NULL,
    profile_id          VARCHAR(255) NOT NULL,
    chat_name           VARCHAR(512) NOT NULL,
    chat_type           VARCHAR(64)  NOT NULL,
    is_favorite         BOOLEAN      NOT NULL DEFAULT FALSE,
    context_type        VARCHAR(255),
    context_id          VARCHAR(255),
    context_label       VARCHAR(512),
    context_entity_type VARCHAR(255),
    created_at          TIMESTAMP    NOT NULL,
    updated_at          TIMESTAMP    NOT NULL
);

CREATE INDEX idx_ai_chat_metadata_user
    ON ai_chat_metadata (user_id);

CREATE UNIQUE INDEX uq_ai_chat_metadata_context
    ON ai_chat_metadata (user_id, context_type, context_id);
