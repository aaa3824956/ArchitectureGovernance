-- AI 解读与修复建议落库（阶段 A）
CREATE TABLE IF NOT EXISTS inspection_ai_suggestion (
    id                 BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    issue_id           BIGINT       NOT NULL,
    kind               VARCHAR(32)  NOT NULL COMMENT 'EXPLAIN_FIX 等',
    model              VARCHAR(64)  NULL,
    prompt_hash        VARCHAR(64)  NULL COMMENT '系统+用户提示摘要 SHA-256',
    input_facts_hash     VARCHAR(64)  NULL COMMENT '事实 JSON 摘要 SHA-256',
    output_json          LONGTEXT     NOT NULL COMMENT '结构化模型输出 JSON 文本',
    raw_assistant_text   TEXT         NULL COMMENT '原始 assistant 文本（审计）',
    duration_ms          BIGINT       NULL,
    created_at           TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_ai_suggestion_issue (issue_id),
    INDEX idx_ai_suggestion_created (created_at)
);
