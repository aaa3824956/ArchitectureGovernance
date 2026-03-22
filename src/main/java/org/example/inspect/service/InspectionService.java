package org.example.inspect.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.inspect.dto.InspectionDTO;
import org.example.inspect.engine.JobEngine;
import org.example.inspect.entity.Inspection;
import org.example.inspect.entity.InspectionJob;
import org.example.inspect.repository.InspectionJobMapper;
import org.example.inspect.repository.InspectionMapper;
import org.example.inspect.repository.RuleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InspectionService {
    @Autowired
    private JobEngine jobEngine;

    @Autowired
    private RuleMapper ruleMapper;

    @Autowired
    private InspectionJobMapper inspectionJobMapper;

    @Autowired
    private InspectionMapper inspectionMapper;

    public void creteInspection(InspectionDTO inspectionDTO) {
        Inspection inspection = new Inspection();
        inspection.setInspectionId(inspectionDTO.getInspectionId());
        inspection.setInspectionName(inspectionDTO.getInspectionName());
        inspection.setDescription(inspection.getDescription());
        inspection.setFunctionId(inspectionDTO.getFunctionId());
        inspectionMapper.insert(inspection);
    }

    public void deleteInspection(String inspectionId) {
        inspectionMapper.deleteById(inspectionId);
    }

    public void startInspection(Long inspectionId){
        LambdaQueryWrapper<InspectionJob> queryWrapper = new LambdaQueryWrapper<InspectionJob>().eq(InspectionJob::getInspectionId, inspectionId);
        InspectionJob inspectionJob = inspectionJobMapper.selectOne(queryWrapper);
        inspectionJob.setStatus("RUNNING");
        inspectionJobMapper.updateById(inspectionJob);
    }

    public void stopInspection(Long inspectionId){
        LambdaQueryWrapper<InspectionJob> queryWrapper = new LambdaQueryWrapper<InspectionJob>().eq(InspectionJob::getInspectionId, inspectionId);
        InspectionJob inspectionJob = inspectionJobMapper.selectOne(queryWrapper);
        inspectionJob.setStatus("STOPPED");
        inspectionJobMapper.updateById(inspectionJob);
    }
}
