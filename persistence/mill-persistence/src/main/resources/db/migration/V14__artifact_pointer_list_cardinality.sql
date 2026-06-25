-- List pointers: allow multiple artifact rows per (chat_id, pointer_key).
-- Rebuild table for portable PK change (PostgreSQL rejects DROP PRIMARY KEY; H2 uses auto-named constraints).
CREATE TABLE ai_chat_artifact_pointer_new (
    chat_id     VARCHAR(255) NOT NULL,
    pointer_key VARCHAR(255) NOT NULL,
    artifact_id VARCHAR(255) NOT NULL REFERENCES ai_chat_artifact (artifact_id) ON DELETE CASCADE,
    updated_at  TIMESTAMP    NOT NULL,
    PRIMARY KEY (chat_id, pointer_key, artifact_id)
);

INSERT INTO ai_chat_artifact_pointer_new (chat_id, pointer_key, artifact_id, updated_at)
SELECT chat_id, pointer_key, artifact_id, updated_at
FROM ai_chat_artifact_pointer;

DROP TABLE ai_chat_artifact_pointer;

ALTER TABLE ai_chat_artifact_pointer_new RENAME TO ai_chat_artifact_pointer;
