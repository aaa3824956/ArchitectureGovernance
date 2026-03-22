package org.example.inspect.rule;

import org.example.inspect.entity.InspectionJob;
import org.springframework.stereotype.Component;

@Component
public class SaasJobExecutor implements JobExecutor {
    @Override
    public String getRuleType() {
        return "saas";
    }

    @Override
    public void execute(Long inspectionId, InspectionJob inspectionJob) {
        inspectionJob.getFunctionId();
        //调用faas函数
    }
}
