-- =============================================================================
-- 业务场景示例：订单核心域「支付结果查询」接口扇入过高
-- 背景：订单服务对外暴露 GET /orders/{id}/payment-status，被购物车、售后、
--       营销发券、客服工单、对账任务等多个上游频繁调用，架构评审要求治理。
-- 使用前请已执行 schema-init.sql、schema-ai-suggestion.sql
-- =============================================================================

INSERT INTO inspection (inspection_name, description)
VALUES (
    '订单域接口治理巡检',
    '针对 order-service 发布单元，扫描扇入过高的 HTTP 读接口，支撑双月架构评审。'
);

SET @ins_id = LAST_INSERT_ID();

INSERT INTO inspection_job (
    inspection_id, status, job_type, rule_param_json, severity, cron_expr,
    next_run_time, locked
) VALUES (
    @ins_id,
    'STOPPED',
    'api_fan_in',
    '{"releaseUnitId":100,"threshold":8}',
    'HIGH',
    '0 0 2 * * ?',
    NOW(6),
    0
);

SET @job_id = LAST_INSERT_ID();

INSERT INTO inspection_issue (
    inspection_id, job_id, object_type, object_id, severity, description, fingerprint, status
) VALUES (
    @ins_id,
    @job_id,
    'API',
    88001,
    'HIGH',
    '接口 PaymentStatusQuery（apiId=88001）扇入过高：被 14 个外部接口调用（阈值 8），调用方微服务: [cart-svc,aftersale-svc,coupon-svc,cs-ticket-svc,reconcile-batch,open-api-gateway,wms-svc,finance-ledger,...]',
    'sf_scenario_payment_status_fanin_88001',
    'OPEN'
);

SELECT issue_id AS demo_issue_id_for_ai FROM inspection_issue ORDER BY issue_id DESC LIMIT 1;
