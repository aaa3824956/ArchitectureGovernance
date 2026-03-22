package org.example.inspect.service;

import org.example.inspect.entity.InspectionJob;
import org.example.inspect.entity.JobExecutionLog;
import org.example.inspect.repository.JobExecutionLogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class JobExecutionLogService {

    @Autowired
    private JobExecutionLogMapper jobExecutionLogMapper;

    public void logSuccess(
            InspectionJob job,
            String nodeId,
            int attemptNumber,
            long durationMs
    ) {
        JobExecutionLog log = base(job, nodeId, attemptNumber, durationMs);
        log.setStatus("SUCCESS");
        log.setWillRetry(false);
        jobExecutionLogMapper.insert(log);
    }

    public void logFailure(
            InspectionJob job,
            String nodeId,
            int attemptNumber,
            long durationMs,
            Throwable error,
            boolean willRetry
    ) {
        JobExecutionLog log = base(job, nodeId, attemptNumber, durationMs);
        log.setStatus("FAILED");
        log.setWillRetry(willRetry);
        log.setErrorMessage(truncateMessage(error));
        jobExecutionLogMapper.insert(log);
    }

    private static JobExecutionLog base(
            InspectionJob job,
            String nodeId,
            int attemptNumber,
            long durationMs
    ) {
        JobExecutionLog log = new JobExecutionLog();
        log.setJobId(job.getJobId());
        log.setInspectionId(job.getInspectionId());
        log.setNodeId(nodeId);
        log.setAttemptNumber(attemptNumber);
        log.setDurationMs(durationMs);
        log.setCreatedAt(LocalDateTime.now());
        return log;
    }

    private static String truncateMessage(Throwable error) {
        String msg = error.getMessage();
        if (msg == null || msg.isEmpty()) {
            msg = error.getClass().getName();
        }
        return msg.length() > 4000 ? msg.substring(0, 4000) : msg;
    }
}
