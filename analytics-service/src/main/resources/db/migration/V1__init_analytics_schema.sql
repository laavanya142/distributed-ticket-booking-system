-- Analytics summary table
CREATE TABLE analytics_summary (
    id           UUID PRIMARY KEY,
    metric_name  VARCHAR(100) NOT NULL,
    metric_value NUMERIC(19, 4) NOT NULL DEFAULT 0,
    last_updated TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_analytics_summary_metric_name UNIQUE (metric_name)
);

CREATE INDEX idx_analytics_summary_metric_name ON analytics_summary(metric_name);
