package org.example.inspect.rule;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
