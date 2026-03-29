CREATE TABLE IF NOT EXISTS inspection (
    inspection_id   BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    inspection_name VARCHAR(128),
    function_id     VARCHAR(64),
    description     TEXT,
    created_at      TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS inspection_rule (
    id              BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    inspection_id   BIGINT,
    rule_id         BIGINT,
    rule_name       VARCHAR(128),
    description     TEXT,
    INDEX idx_rule_inspection_id (inspection_id)
);

CREATE TABLE IF NOT EXISTS inspection_job (
    job_id                 BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    inspection_id          BIGINT       NOT NULL,
    function_id            BIGINT,
    rule_id                BIGINT,
    cron_expr              VARCHAR(64),
    status                 VARCHAR(32)  NOT NULL DEFAULT 'RUNNING',
    job_type               VARCHAR(64),
    rule_param_json        TEXT,
    severity               VARCHAR(16)  NOT NULL DEFAULT 'HIGH',
    claim_token            VARCHAR(64),
    max_retry              INT          NOT NULL DEFAULT 3,
    retry_count            INT          NOT NULL DEFAULT 0,
    retry_interval_seconds INT          NOT NULL DEFAULT 60,
    locked                 TINYINT(1)   NOT NULL DEFAULT 0,
    lock_node              VARCHAR(64),
    lock_time              DATETIME(6),
    next_run_time          DATETIME(6),
    last_run_time          DATETIME(6),
    created_at             TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at             TIMESTAMP    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_job_status_next (status, next_run_time),
    INDEX idx_job_inspection  (inspection_id)
);

CREATE TABLE IF NOT EXISTS inspection_issue (
    issue_id       BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    inspection_id  BIGINT       NOT NULL,
    job_id         BIGINT       NOT NULL,
    object_type    VARCHAR(32),
    object_id      BIGINT,
    severity       VARCHAR(16)  NOT NULL DEFAULT 'HIGH',
    description    TEXT,
    fingerprint    VARCHAR(64),
    status         VARCHAR(16)  NOT NULL DEFAULT 'OPEN',
    created_at     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_issue_job_id      (job_id),
    INDEX idx_issue_inspection  (inspection_id),
    INDEX idx_issue_fingerprint (fingerprint),
    INDEX idx_issue_status      (status)
);

CREATE TABLE IF NOT EXISTS inspection_job_execution_log (
    id               BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    job_id           BIGINT       NOT NULL,
    inspection_id    BIGINT       NOT NULL,
    node_id          VARCHAR(64),
    attempt_number   INT          NOT NULL DEFAULT 1,
    status           VARCHAR(16)  NOT NULL,
    duration_ms      BIGINT,
    error_message    TEXT,
    will_retry       TINYINT(1)   DEFAULT 0,
    created_at       TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_log_job_id       (job_id),
    INDEX idx_log_inspection   (inspection_id),
    INDEX idx_log_created_at   (created_at)
);

CREATE TABLE IF NOT EXISTS api (
    api_id         BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    api_name       VARCHAR(256),
    release_unit_id INT,
    created_at     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS api_dependency (
    api_dependency_id VARCHAR(64) NOT NULL PRIMARY KEY,
    from_release_unit_id VARCHAR(64),
    from_api_id      VARCHAR(64),
    to_api_id        VARCHAR(64),
    to_release_unit_id VARCHAR(64),
    created_at       TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_dep_to_unit (to_release_unit_id),
    INDEX idx_dep_to_api  (to_api_id)
);
