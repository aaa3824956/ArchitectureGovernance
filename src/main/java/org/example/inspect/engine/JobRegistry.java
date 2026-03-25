package org.example.inspect.engine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JobRegistry {
    private Map<String, JobExecutor> ruleExecutorMap = new HashMap();

    @Autowired
    public JobRegistry(List<JobExecutor> jobExecutors) {
        for (JobExecutor jobExecutor : jobExecutors) {
            ruleExecutorMap.put(jobExecutor.getRuleType(), jobExecutor);
        }
    }

    public JobExecutor getRuleExecutor(String ruleType) {
        return ruleExecutorMap.get(ruleType);
    }
}
