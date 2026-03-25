package org.example.inspect.service;

import org.example.inspect.common.CronUtils;
import org.example.inspect.config.InspectionProperties;
import org.example.inspect.engine.JobEngine;
import org.example.inspect.entity.InspectionJob;
import org.example.inspect.repository.InspectionJobMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

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

    public List<InspectionJob> claimDueJobs() {
        String nodeId = resolveNodeId();
        int limit = inspectionProperties.getScheduling().getBatchSize();
        String claimToken = UUID.randomUUID().toString();
        int claimed = inspectionJobMapper.claimDueJobs(nodeId, claimToken, limit);
        if (claimed <= 0) {
            return Collections.emptyList();
        }
        return inspectionJobMapper.findClaimedJobs(nodeId, claimToken, limit);
    }

    public void runInspectionJob(InspectionJob inspectionJob) {
        long startMs = System.currentTimeMillis();
        String nodeId = resolveNodeId();
        Integer maxRetryCfg = inspectionJob.getMaxRetry();
        int maxRetry = maxRetryCfg == null ? 3 : maxRetryCfg;
        Integer retryIntervalCfg = inspectionJob.getRetryIntervalSeconds();
        int retryInterval =
                retryIntervalCfg == null
                        ? 60
                        : retryIntervalCfg;
        Integer retryCount = inspectionJob.getRetryCount();
        int priorFailures = retryCount == null ? 0 : retryCount;

        try {
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
            inspectionJobMapper.unlock(inspectionJob.getJobId(), nodeId);
        }
    }

    public void releaseTimeoutLocks() {
        inspectionJobMapper.releaseTimeoutLocks();
    }

    private static String resolveNodeId() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return "unknown";
        }
    }
}
