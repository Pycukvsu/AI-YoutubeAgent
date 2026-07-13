CREATE TABLE mini_series (
    id              BIGSERIAL PRIMARY KEY,
    title           VARCHAR(200) NOT NULL,
    concept         TEXT,
    total_episodes  INTEGER NOT NULL DEFAULT 5,
    current_episode INTEGER NOT NULL DEFAULT 0,
    status          VARCHAR(50) DEFAULT 'created',
    episodes_json   TEXT,
    category        VARCHAR(100),
    language        VARCHAR(50) DEFAULT 'ru',
    created_at      TIMESTAMP DEFAULT NOW(),
    completed_at    TIMESTAMP
);

CREATE INDEX idx_mini_series_status ON mini_series(status);
