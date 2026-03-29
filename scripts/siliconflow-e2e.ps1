# 端到端：执行 db/seed-ai-demo-fixed-ids.sql 后，应用使用 profile=siliconflow 启动，再运行本脚本
# 用法: .\scripts\siliconflow-e2e.ps1   （默认 issueId=9999003）
# 环境变量 SILICONFLOW_API_KEY 由你在系统或当前终端中事先配置（勿写入仓库）

param(
    [int] $IssueId = 9999003,
    [string] $BaseUrl = "http://localhost:8080"
)

$status = Invoke-RestMethod -Uri "$BaseUrl/inspection/ai/status" -Method Get
Write-Host "AI status:" ($status | ConvertTo-Json -Compress)
if ($status.demoMode -eq $true) {
    Write-Warning "当前为 demo-mode，不会请求硅基流动。请使用 profile=siliconflow 且 demo-mode=false。"
}
if ($status.enabled -ne $true) {
    Write-Warning "inspection.ai.enabled 未开启，请使用 application-siliconflow.yml（--spring.profiles.active=siliconflow）"
}

$uri = "$BaseUrl/inspection/issues/$IssueId/ai/suggest"
$body = @{
    userNote = "业务上支付状态查询与订单详情耦合，各域为省事直接调同一接口。请给出可落地的拆分与迁移建议。"
} | ConvertTo-Json -Compress

Write-Host "POST $uri"
$r = Invoke-RestMethod -Method Post -Uri $uri -ContentType "application/json; charset=utf-8" -Body $body
Write-Host "model=$($r.model) durationMs=$($r.durationMs) id=$($r.id)"
Write-Host "outputJson:"
Write-Host $r.outputJson
