-- =============================================================================
-- AI 演示用种子数据（固定主键，可反复执行）
-- =============================================================================
-- 前置：已执行 schema-init.sql；inspection_ai_suggestion 表已存在（schema-ai-suggestion.sql）
-- 若 inspection_issue / inspection_job 尚未有 v3 迁移列，请先执行 schema-migration-v3.sql
--
-- 固定 ID（文档与脚本可直接写死）：
--   inspection_id = 9999001
--   job_id         = 9999002
--   issue_id       = 9999003  ← 调用 AI 接口时用此 ID
--
-- 演示请求示例：
--   POST http://localhost:8080/inspection/issues/9999003/ai/suggest
--   Content-Type: application/json
--   {"userNote":"请基于事实给出治理建议"}
-- =============================================================================

START TRANSACTION;

DELETE FROM inspection_ai_suggestion WHERE issue_id = 9999003;
DELETE FROM inspection_issue WHERE issue_id = 9999003;
DELETE FROM inspection_job WHERE job_id = 9999002;
DELETE FROM inspection WHERE inspection_id = 9999001;

INSERT INTO inspection (inspection_id, inspection_name, description)
VALUES (
    9999001,
    'AI演示-订单域治理',
    '架构巡检 + AI 解读演示：api_fan_in 规则 + 支付查询接口扇入场景。'
);

INSERT INTO inspection_job (
    job_id,
    inspection_id,
    status,
    job_type,
    rule_param_json,
    severity,
    cron_expr,
    next_run_time,
    locked
) VALUES (
    9999002,
    9999001,
    'STOPPED',
    'api_fan_in',
    '{"releaseUnitId":100,"threshold":8}',
    'HIGH',
    '0 0 2 * * ?',
    NOW(6),
    0
);

INSERT INTO inspection_issue (
    issue_id,
    inspection_id,
    job_id,
    object_type,
    object_id,
    severity,
    description,
    fingerprint,
    status
) VALUES (
    9999003,
    9999001,
    9999002,
    'API',
    88001,
    'HIGH',
    '接口 PaymentStatusQuery（apiId=88001）扇入过高：被 14 个外部接口调用（阈值 8），调用方微服务: [cart-svc,aftersale-svc,coupon-svc,cs-ticket-svc,reconcile-batch,open-api-gateway]',
    'seed_demo_payment_status_88001',
    'OPEN'
);

COMMIT;

SELECT
    9999003 AS demo_issue_id,
    9999001 AS demo_inspection_id,
    9999002 AS demo_job_id,
    'POST /inspection/issues/9999003/ai/suggest' AS next_step;
