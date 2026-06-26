-- WI-360: artefact lifecycle status + facet assignment provenance

ALTER TABLE ai_chat_artifact
    ADD COLUMN status VARCHAR(32) NOT NULL DEFAULT 'pending';

ALTER TABLE metadata_entity_facet
    ADD COLUMN source_artifact_id VARCHAR(255);

CREATE INDEX idx_metadata_entity_facet_source_artifact
    ON metadata_entity_facet (source_artifact_id);
