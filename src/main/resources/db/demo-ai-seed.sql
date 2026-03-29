-- AI 接口演示用种子数据（在已执行 schema-init.sql、schema-ai-suggestion.sql 后执行）
-- 推荐改用 seed-ai-demo-fixed-ids.sql：固定 issue_id=9999003，文档与脚本无需每次改 ID。
-- 本文件保留为「自增主键」版本；执行后请记下最后一行查询出的 demo_issue_id

INSERT INTO inspection (inspection_name, description)
VALUES ('Demo巡检', 'AI 演示用');

SET @ins_id = LAST_INSERT_ID();

INSERT INTO inspection_job (
    inspection_id, status, job_type, rule_param_json, severity, cron_expr,
    next_run_time, locked
) VALUES (
    @ins_id, 'STOPPED', 'api_fan_in', '{"releaseUnitId":1,"threshold":5}', 'HIGH',
    '0 0 * * * ?', NOW(6), 0
);

SET @job_id = LAST_INSERT_ID();

INSERT INTO inspection_issue (
    inspection_id, job_id, object_type, object_id, severity, description, fingerprint, status
) VALUES (
    @ins_id, @job_id, 'API', 10001, 'HIGH',
    '接口 OrderQueryApi 扇入过高: 被 12 个外部接口调用（阈值 5），调用方微服务: [2,3,7]',
    'demo_fp_order_query_001', 'OPEN'
);

SELECT issue_id AS demo_issue_id FROM inspection_issue ORDER BY issue_id DESC LIMIT 1;
