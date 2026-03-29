# 架构巡检 AI 演示脚本（需先启动应用且启用 demo profile）
# 1) 启动: mvn spring-boot:run "-Dspring-boot.run.profiles=demo"
# 2) 在 MySQL 执行: src/main/resources/db/seed-ai-demo-fixed-ids.sql（固定 issue_id=9999003）

param(
    [int] $IssueId = 9999003,
    [string] $BaseUrl = "http://localhost:8080"
)

if ($IssueId -le 0) {
    Write-Host "请执行 db/seed-ai-demo-fixed-ids.sql，或传入 -IssueId（默认 9999003）。"
    exit 1
}

$uri = "$BaseUrl/inspection/issues/$IssueId/ai/suggest"
Write-Host "POST $uri"
$body = '{"userNote":"演示：希望优先评估对订单核心域的影响"}'
try {
    $r = Invoke-RestMethod -Method Post -Uri $uri -ContentType "application/json; charset=utf-8" -Body $body
    Write-Host "成功。model=$($r.model) durationMs=$($r.durationMs)"
    Write-Host "outputJson:"
    Write-Host $r.outputJson
} catch {
    Write-Host $_
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        Write-Host $reader.ReadToEnd()
    }
    exit 1
}

$latest = "$BaseUrl/inspection/issues/$IssueId/ai/latest?kind=EXPLAIN_FIX"
Write-Host ""
Write-Host "GET $latest"
Invoke-RestMethod -Method Get -Uri $latest | ConvertTo-Json -Depth 6
