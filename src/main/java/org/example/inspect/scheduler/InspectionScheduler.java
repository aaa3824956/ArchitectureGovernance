package org.example.inspect.scheduler;

import org.example.inspect.entity.InspectionJob;
import org.example.inspect.service.InspectionJobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.Executor;

@Component
public class InspectionScheduler {

    @Autowired
    private InspectionJobService inspectionJobService;

    @Autowired
    @Qualifier("inspectionJobExecutor")
    private Executor inspectionJobExecutor;

    @Scheduled(fixedDelayString = "${inspection.scheduling.scan-interval-ms:10000}")
    public void schedule() {
        cleanLocks();
        List<InspectionJob> jobs = inspectionJobService.findDueJobs();
        for (InspectionJob job : jobs) {
            InspectionJob snapshot = job;
            inspectionJobExecutor.execute(() -> inspectionJobService.runInspectionJob(snapshot));
        }
    }

    private void cleanLocks() {
        inspectionJobService.releaseTimeoutLocks();
    }
}
