package org.example.inspect.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 显式实现 jobType / ruleParamJson / severity 的访问器，避免仅依赖 {@code @Data} 时 IDE 或部分编译环境报「找不到 getJobType()」。
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("inspection_job")
public class InspectionJob {

    @TableId(type = IdType.AUTO)
    private Long jobId;

    private Long inspectionId;

    private Long functionId;

    private Long ruleId;

    private String cronExpr;

    private String status;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private String jobType;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private String ruleParamJson;

    /** 问题严重程度：BLOCKER / HIGH / MEDIUM / LOW */
    @Builder.Default
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private String severity = "HIGH";

    /** 本轮批量认领的唯一标识，避免并发场景误取任务 */
    private String claimToken;

    /** 连续失败允许的最大次数（含），超过后按 Cron 进入下一轮 */
    @Builder.Default
    private Integer maxRetry = 3;

    /** 当前连续失败次数 */
    @Builder.Default
    private Integer retryCount = 0;

    /** 重试基准间隔（秒），实际间隔为指数退避 */
    @Builder.Default
    private Integer retryIntervalSeconds = 60;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public String getJobType() {
        return jobType;
    }

    public void setJobType(String jobType) {
        this.jobType = jobType;
    }

    public String getRuleParamJson() {
        return ruleParamJson;
    }

    public void setRuleParamJson(String ruleParamJson) {
        this.ruleParamJson = ruleParamJson;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }
}
