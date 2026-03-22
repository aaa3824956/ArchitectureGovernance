package org.example.inspect.engine;

import org.example.inspect.entity.InspectionJob;
import org.example.inspect.rule.JobExecutor;
import org.example.inspect.rule.JobRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JobEngine {
    @Autowired
    private JobRegistry jobRegistry;

    public void executeRule(Long inspectionId,
                            InspectionJob inspectionJob){
        JobExecutor jobExecutor = jobRegistry.getRuleExecutor(inspectionJob.getJobType());
        if (jobExecutor == null) {
            throw new IllegalStateException(
                    "No JobExecutor for jobType=" + inspectionJob.getJobType());
        }
        jobExecutor.execute(inspectionId, inspectionJob);
    }
}
