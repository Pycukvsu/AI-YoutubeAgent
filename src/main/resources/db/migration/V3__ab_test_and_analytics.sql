CREATE TABLE ab_test (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(200) NOT NULL,
    description     VARCHAR(500),
    status          VARCHAR(50) DEFAULT 'active',
    test_type       VARCHAR(50) NOT NULL,
    variants_json   JSONB,
    results_json    JSONB,
    created_at      TIMESTAMP DEFAULT NOW(),
    completed_at    TIMESTAMP
);

CREATE TABLE video_analytics (
    id                      BIGSERIAL PRIMARY KEY,
    video_id                BIGINT REFERENCES video(id),
    views                   BIGINT DEFAULT 0,
    likes                   BIGINT DEFAULT 0,
    comments                INTEGER DEFAULT 0,
    shares                  INTEGER DEFAULT 0,
    subscribers_gained      INTEGER DEFAULT 0,
    watch_time_minutes      DECIMAL(10,2) DEFAULT 0,
    avg_view_duration_seconds DECIMAL(10,2) DEFAULT 0,
    click_through_rate      DECIMAL(5,2) DEFAULT 0,
    variant                 VARCHAR(50),
    recorded_at             TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_ab_test_status ON ab_test(status);
CREATE INDEX idx_ab_test_type ON ab_test(test_type);
CREATE INDEX idx_video_analytics_video ON video_analytics(video_id, recorded_at DESC);
CREATE INDEX idx_video_analytics_variant ON video_analytics(variant);
