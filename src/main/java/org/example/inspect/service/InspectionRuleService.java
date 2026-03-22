package org.example.inspect.service;

import org.example.inspect.entity.InspectionJob;
import org.example.inspect.entity.InspectionRule;
import org.example.inspect.repository.InspectionJobMapper;
import org.example.inspect.repository.InspectionRuleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InspectionRuleService {
    @Autowired
    private InspectionRuleMapper inspectionRuleMapper;

    @Autowired
    private InspectionJobMapper jobMapper;
    @Autowired
    private InspectionJobMapper inspectionJobMapper;

    public void bindRule(
            Long inspectionId,
            Long ruleId,
            String cronExpr){

        InspectionRule rule =
                new InspectionRule();

        rule.setInspectionId(inspectionId);
        rule.setRuleId(ruleId);
        inspectionRuleMapper.insert(rule);

        InspectionJob job = InspectionJob.builder()
                .inspectionId(inspectionId)
                .ruleId(ruleId)
                .cronExpr(cronExpr)
                .status("STOPPED")
                .build();
        inspectionJobMapper.insert(job);
    }
}
