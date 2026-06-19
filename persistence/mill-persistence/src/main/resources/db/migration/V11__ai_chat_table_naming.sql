-- WI-323: Cosmetic renames — align chat satellite tables under ai_chat_* prefix.

-- chat_memory pair: drop child FK, rename, recreate FK + indexes
ALTER TABLE chat_memory_message DROP CONSTRAINT fk_chat_memory_message_chat;

ALTER TABLE chat_memory RENAME TO ai_chat_memory;
ALTER TABLE chat_memory_message RENAME TO ai_chat_memory_message;

ALTER TABLE ai_chat_memory_message
    ADD CONSTRAINT fk_ai_chat_memory_message_chat
    FOREIGN KEY (chat_id) REFERENCES ai_chat_memory (chat_id) ON DELETE CASCADE;

DROP INDEX IF EXISTS idx_chat_memory_profile;
CREATE INDEX idx_ai_chat_memory_profile ON ai_chat_memory (profile_id);

DROP INDEX IF EXISTS idx_chat_memory_message_chat;
CREATE INDEX idx_ai_chat_memory_message_chat ON ai_chat_memory_message (chat_id, position);

-- Artefacts, pointers, run telemetry
ALTER TABLE ai_artifact RENAME TO ai_chat_artifact;
ALTER TABLE ai_active_artifact_pointer RENAME TO ai_chat_artifact_pointer;
ALTER TABLE ai_run_event RENAME TO ai_chat_run_event;

DROP INDEX IF EXISTS idx_ai_artifact_chat;
CREATE INDEX idx_ai_chat_artifact_chat ON ai_chat_artifact (chat_id, created_at);

DROP INDEX IF EXISTS idx_ai_artifact_turn;
CREATE INDEX idx_ai_chat_artifact_turn ON ai_chat_artifact (turn_id);

DROP INDEX IF EXISTS idx_ai_run_event_chat;
CREATE INDEX idx_ai_chat_run_event_chat ON ai_chat_run_event (chat_id, created_at);

DROP INDEX IF EXISTS idx_ai_run_event_run;
CREATE INDEX idx_ai_chat_run_event_run ON ai_chat_run_event (run_id, created_at);
