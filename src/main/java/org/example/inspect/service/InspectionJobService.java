package org.example.inspect.service;

import org.example.inspect.common.CronUtils;
import org.example.inspect.config.InspectionProperties;
import org.example.inspect.engine.JobEngine;
import org.example.inspect.entity.InspectionJob;
import org.example.inspect.repository.InspectionJobMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class InspectionJobService {
    @Autowired
    private InspectionJobMapper inspectionJobMapper;

    @Autowired
    private JobEngine jobEngine;

    @Autowired
    private JobExecutionLogService jobExecutionLogService;

    @Autowired
    private InspectionProperties inspectionProperties;

    public List<InspectionJob> findDueJobs() {
        int limit = inspectionProperties.getScheduling().getBatchSize();
        return inspectionJobMapper.findDueJobs(limit);
    }

    public void runInspectionJob(InspectionJob inspectionJob) {
        long startMs = System.currentTimeMillis();
        String nodeId = resolveNodeId();
        boolean locked = false;
        int maxRetry = inspectionJob.getMaxRetry() != null ? inspectionJob.getMaxRetry() : 3;
        int retryInterval =
                inspectionJob.getRetryIntervalSeconds() != null
                        ? inspectionJob.getRetryIntervalSeconds()
                        : 60;
        int priorFailures = inspectionJob.getRetryCount() != null ? inspectionJob.getRetryCount() : 0;

        try {
            int lockRows = inspectionJobMapper.tryLock(inspectionJob.getJobId(), nodeId);
            if (lockRows == 0) {
                return;
            }
            locked = true;

            jobEngine.executeRule(inspectionJob.getInspectionId(), inspectionJob);

            inspectionJobMapper.resetRetryCount(inspectionJob.getJobId());
            LocalDateTime nextRunTime = CronUtils.calcNextRunTime(inspectionJob.getCronExpr());
            inspectionJobMapper.updateNextRunTime(
                    inspectionJob.getJobId(),
                    nextRunTime,
                    LocalDateTime.now()
            );

            long duration = System.currentTimeMillis() - startMs;
            jobExecutionLogService.logSuccess(
                    inspectionJob,
                    nodeId,
                    priorFailures + 1,
                    duration
            );
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startMs;
            int failOrdinal = priorFailures + 1;
            if (failOrdinal <= maxRetry) {
                LocalDateTime retryAt =
                        CronUtils.calcRetryAfter(LocalDateTime.now(), retryInterval, failOrdinal);
                inspectionJobMapper.scheduleRetry(inspectionJob.getJobId(), failOrdinal, retryAt);
                jobExecutionLogService.logFailure(
                        inspectionJob,
                        nodeId,
                        failOrdinal,
                        duration,
                        e,
                        true
                );
            } else {
                inspectionJobMapper.resetRetryCount(inspectionJob.getJobId());
                LocalDateTime nextRunTime = CronUtils.calcNextRunTime(inspectionJob.getCronExpr());
                inspectionJobMapper.updateNextRunTime(
                        inspectionJob.getJobId(),
                        nextRunTime,
                        LocalDateTime.now()
                );
                jobExecutionLogService.logFailure(
                        inspectionJob,
                        nodeId,
                        failOrdinal,
                        duration,
                        e,
                        false
                );
            }
        } finally {
            if (locked) {
                inspectionJobMapper.unlock(inspectionJob.getJobId(), nodeId);
            }
        }
    }

    public void releaseTimeoutLocks() {
        inspectionJobMapper.releaseTimeoutLocks();
    }

    private static String resolveNodeId() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return "unknown";
        }
    }
}
