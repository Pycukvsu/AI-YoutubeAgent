CREATE INDEX idx_trend_status ON trend(status);
CREATE INDEX idx_trend_discovered ON trend(discovered_at DESC);
CREATE INDEX idx_video_status ON video(status);
CREATE INDEX idx_video_youtube_id ON video(youtube_id);
CREATE INDEX idx_video_scheduled ON video(scheduled_at) WHERE status = 'pending';
CREATE INDEX idx_pipeline_run_video ON pipeline_run(video_id);
CREATE INDEX idx_analytics_video ON analytics_snapshot(video_id, snapshot_at DESC);
