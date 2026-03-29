package org.example.inspect.dto;

import java.time.LocalDateTime;

/** 返回给前端的 AI 建议视图 */
public class AiSuggestionView {

    private Long id;
    private Long issueId;
    private String kind;
    private String model;
    /** 结构化 JSON 字符串，前端可自行 parse */
    private String outputJson;
    private Long durationMs;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getIssueId() { return issueId; }
    public void setIssueId(Long issueId) { this.issueId = issueId; }

    public String getKind() { return kind; }
    public void setKind(String kind) { this.kind = kind; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getOutputJson() { return outputJson; }
    public void setOutputJson(String outputJson) { this.outputJson = outputJson; }

    public Long getDurationMs() { return durationMs; }
    public void setDurationMs(Long durationMs) { this.durationMs = durationMs; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
