package org.example.inspect.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("inspection_job_execution_log")
public class JobExecutionLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long jobId;

    private Long inspectionId;

    private String nodeId;

    private Integer attemptNumber;

    /** SUCCESS / FAILED */
    private String status;

    private Long durationMs;

    private String errorMessage;

    /** 失败后是否还会按计划重试（仅 FAILED 时有意义） */
    private Boolean willRetry;

    private LocalDateTime createdAt;
}
