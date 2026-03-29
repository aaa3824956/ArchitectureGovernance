package org.example.inspect.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("inspection_ai_suggestion")
public class AiSuggestion {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long issueId;
    private String kind;
    private String model;
    private String promptHash;
    private String inputFactsHash;
    private String outputJson;
    private String rawAssistantText;
    private Long durationMs;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getIssueId() { return issueId; }
    public void setIssueId(Long issueId) { this.issueId = issueId; }

    public String getKind() { return kind; }
    public void setKind(String kind) { this.kind = kind; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getPromptHash() { return promptHash; }
    public void setPromptHash(String promptHash) { this.promptHash = promptHash; }

    public String getInputFactsHash() { return inputFactsHash; }
    public void setInputFactsHash(String inputFactsHash) { this.inputFactsHash = inputFactsHash; }

    public String getOutputJson() { return outputJson; }
    public void setOutputJson(String outputJson) { this.outputJson = outputJson; }

    public String getRawAssistantText() { return rawAssistantText; }
    public void setRawAssistantText(String rawAssistantText) { this.rawAssistantText = rawAssistantText; }

    public Long getDurationMs() { return durationMs; }
    public void setDurationMs(Long durationMs) { this.durationMs = durationMs; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
