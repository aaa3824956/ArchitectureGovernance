-- 执行日志表 + 巡检任务重试字段（在目标库中手工执行一次）

CREATE TABLE IF NOT EXISTS inspection_job_execution_log (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    job_id BIGINT NOT NULL,
    inspection_id BIGINT NOT NULL,
    node_id VARCHAR(64),
    attempt_number INT NOT NULL DEFAULT 1,
    status VARCHAR(16) NOT NULL,
    duration_ms BIGINT,
    error_message TEXT,
    will_retry TINYINT(1) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_job_id (job_id),
    INDEX idx_inspection_id (inspection_id),
    INDEX idx_created_at (created_at)
);

-- 若列已存在会报错，按需注释掉已执行过的语句
ALTER TABLE inspection_job
    ADD COLUMN max_retry INT NOT NULL DEFAULT 3,
    ADD COLUMN retry_count INT NOT NULL DEFAULT 0,
    ADD COLUMN retry_interval_seconds INT NOT NULL DEFAULT 60;

-- 建议为调度查询补充索引（按实际表结构调整列名）
-- CREATE INDEX idx_job_due ON inspection_job (status, next_run_time);
