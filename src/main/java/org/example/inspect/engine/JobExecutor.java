package org.example.inspect.engine;

import org.example.inspect.entity.InspectionJob;

public interface JobExecutor {
    String getRuleType();
    void execute(Long inspectionId, InspectionJob inspectionJob);
}
