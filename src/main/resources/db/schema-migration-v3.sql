-- v3: inspection_issue 表增加 fingerprint / status 列，支持问题去重与状态流转

ALTER TABLE inspection_issue
    ADD COLUMN fingerprint VARCHAR(64) NULL COMMENT '问题唯一指纹（SHA-256），用于跨批次去重',
    ADD COLUMN status      VARCHAR(16) NOT NULL DEFAULT 'OPEN' COMMENT '问题状态: OPEN / NEW / RESOLVED / WONT_FIX',
    ADD COLUMN created_at  TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN updated_at  TIMESTAMP   DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

CREATE INDEX idx_issue_job_id      ON inspection_issue (job_id);
CREATE INDEX idx_issue_fingerprint ON inspection_issue (fingerprint);
CREATE INDEX idx_issue_status      ON inspection_issue (status);

-- inspection_job 表增加 severity 列
ALTER TABLE inspection_job
    ADD COLUMN severity VARCHAR(16) NOT NULL DEFAULT 'HIGH' COMMENT '问题严重程度: BLOCKER / HIGH / MEDIUM / LOW';

-- 若 inspection_issue 表尚未创建，可用下面的完整建表语句代替上面的 ALTER
-- CREATE TABLE IF NOT EXISTS inspection_issue (
--     issue_id       BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
--     inspection_id  BIGINT       NOT NULL,
--     job_id         BIGINT       NOT NULL,
--     object_type    VARCHAR(32),
--     object_id      BIGINT,
--     severity       VARCHAR(16)  NOT NULL DEFAULT 'HIGH',
--     description    TEXT,
--     fingerprint    VARCHAR(64)  NULL     COMMENT '问题唯一指纹',
--     status         VARCHAR(16)  NOT NULL DEFAULT 'OPEN',
--     created_at     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
--     updated_at     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
--     INDEX idx_issue_job_id      (job_id),
--     INDEX idx_issue_inspection  (inspection_id),
--     INDEX idx_issue_fingerprint (fingerprint),
--     INDEX idx_issue_status      (status)
-- );
