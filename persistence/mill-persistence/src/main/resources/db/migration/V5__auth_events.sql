CREATE TABLE auth_events (
    event_id      VARCHAR(255) PRIMARY KEY,
    event_type    VARCHAR(64)  NOT NULL,
    user_id       VARCHAR(255),
    subject       VARCHAR(512),
    ip_address    VARCHAR(64),
    user_agent    VARCHAR(1024),
    failure_reason VARCHAR(255),
    occurred_at   TIMESTAMP    NOT NULL
);

CREATE INDEX idx_auth_events_user_id     ON auth_events (user_id);
CREATE INDEX idx_auth_events_event_type  ON auth_events (event_type);
CREATE INDEX idx_auth_events_occurred_at ON auth_events (occurred_at);
