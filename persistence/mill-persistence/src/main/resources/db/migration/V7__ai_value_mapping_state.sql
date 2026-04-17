-- WI-184: per-attribute value-mapping refresh state for operators and WI-182 orchestrator.

-- TIMESTAMP (not TIMESTAMPTZ): matches V6 style and H2 testIT (MODE=PostgreSQL); Postgres accepts TIMESTAMP for these columns.
CREATE TABLE ai_value_mapping_state (
    entity_res VARCHAR(512) NOT NULL PRIMARY KEY,
    last_refresh_at TIMESTAMP,
    next_scheduled_refresh_at TIMESTAMP,
    last_refresh_values_count BIGINT,
    last_refresh_status VARCHAR(32) NOT NULL DEFAULT 'FAILED',
    current_state VARCHAR(32) NOT NULL DEFAULT 'IDLE',
    refresh_progress_percent INT,
    status_detail TEXT,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
