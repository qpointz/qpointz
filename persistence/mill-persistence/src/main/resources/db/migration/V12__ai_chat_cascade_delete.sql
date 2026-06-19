-- WI-324: ON DELETE CASCADE from ai_chat for satellite tables (orphan cleanup + FK add).

-- Remove orphan rows before FK enforcement
DELETE FROM ai_chat_memory_message m
WHERE NOT EXISTS (SELECT 1 FROM ai_chat c WHERE c.chat_id = m.chat_id);

DELETE FROM ai_chat_memory mem
WHERE NOT EXISTS (SELECT 1 FROM ai_chat c WHERE c.chat_id = mem.chat_id);

DELETE FROM ai_chat_artifact_pointer p
WHERE NOT EXISTS (SELECT 1 FROM ai_chat c WHERE c.chat_id = p.chat_id);

DELETE FROM ai_chat_artifact a
WHERE NOT EXISTS (SELECT 1 FROM ai_chat c WHERE c.chat_id = a.chat_id);

DELETE FROM ai_chat_run_event e
WHERE e.chat_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM ai_chat c WHERE c.chat_id = e.chat_id);

DELETE FROM ai_chat_turn t
WHERE NOT EXISTS (SELECT 1 FROM ai_chat c WHERE c.chat_id = t.chat_id);

ALTER TABLE ai_chat_memory
    ADD CONSTRAINT fk_ai_chat_memory_chat
    FOREIGN KEY (chat_id) REFERENCES ai_chat (chat_id) ON DELETE CASCADE;

ALTER TABLE ai_chat_artifact
    ADD CONSTRAINT fk_ai_chat_artifact_chat
    FOREIGN KEY (chat_id) REFERENCES ai_chat (chat_id) ON DELETE CASCADE;

ALTER TABLE ai_chat_run_event
    ADD CONSTRAINT fk_ai_chat_run_event_chat
    FOREIGN KEY (chat_id) REFERENCES ai_chat (chat_id) ON DELETE CASCADE;
