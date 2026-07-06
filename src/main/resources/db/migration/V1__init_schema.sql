CREATE TABLE trend (
    id              BIGSERIAL PRIMARY KEY,
    topic           VARCHAR(500) NOT NULL,
    source          VARCHAR(50) NOT NULL,
    search_volume   INTEGER,
    category        VARCHAR(100),
    raw_data        JSONB,
    status          VARCHAR(20) DEFAULT 'new',
    discovered_at   TIMESTAMP DEFAULT NOW(),
    used_at         TIMESTAMP,
    expires_at      TIMESTAMP
);

CREATE TABLE video (
    id              BIGSERIAL PRIMARY KEY,
    trend_id        BIGINT REFERENCES trend(id),
    title           VARCHAR(200) NOT NULL,
    description     TEXT,
    tags            TEXT[],
    youtube_id      VARCHAR(50),
    youtube_url     VARCHAR(200),
    status          VARCHAR(30) DEFAULT 'pending',
    file_path       VARCHAR(500),
    thumbnail_path  VARCHAR(500),
    duration_seconds INTEGER,
    scheduled_at    TIMESTAMP,
    uploaded_at     TIMESTAMP,
    created_at      TIMESTAMP DEFAULT NOW(),
    updated_at      TIMESTAMP DEFAULT NOW()
);

CREATE TABLE script (
    id              BIGSERIAL PRIMARY KEY,
    video_id        BIGINT REFERENCES video(id),
    content         TEXT NOT NULL,
    word_count      INTEGER,
    duration_estimate_seconds INTEGER,
    tone            VARCHAR(50),
    openai_model    VARCHAR(50),
    openai_tokens_used INTEGER,
    generated_at    TIMESTAMP DEFAULT NOW()
);

CREATE TABLE pipeline_run (
    id              BIGSERIAL PRIMARY KEY,
    video_id        BIGINT REFERENCES video(id),
    stage           VARCHAR(50),
    status          VARCHAR(20) DEFAULT 'running',
    error_message   TEXT,
    retry_count     INTEGER DEFAULT 0,
    started_at      TIMESTAMP DEFAULT NOW(),
    completed_at    TIMESTAMP,
    stages_json     JSONB
);

CREATE TABLE analytics_snapshot (
    id              BIGSERIAL PRIMARY KEY,
    video_id        BIGINT REFERENCES video(id),
    youtube_video_id VARCHAR(50),
    views           BIGINT DEFAULT 0,
    likes           BIGINT DEFAULT 0,
    comments        INTEGER DEFAULT 0,
    subscribers_gained INTEGER DEFAULT 0,
    watch_time_minutes DECIMAL(10,2) DEFAULT 0,
    snapshot_at     TIMESTAMP DEFAULT NOW()
);
