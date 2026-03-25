package org.example.inspect.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InspectionJob {

    private Long jobId;

    private Long inspectionId;

    private Long functionId;

    private Long ruleId;

    private String cronExpr;

    private String status;

    private String jobType;

    private String ruleParamJson;

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
}
