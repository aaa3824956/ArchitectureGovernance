package org.example.inspect.engine;

import java.util.List;

import org.example.inspect.dto.ApiFanInDTO;
import org.example.inspect.entity.InspectionIssue;
import org.example.inspect.entity.InspectionJob;
import org.example.inspect.repository.ApiDependencyMapper;
import org.example.inspect.repository.InspectionIssueMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

@Component
public class ApiFanInJobExecutor implements JobExecutor {

    @Autowired
    private ApiDependencyMapper mapper;

    @Autowired
    private InspectionIssueMapper issueMapper;

    @Override
    public String getRuleType() {
        return "api_fan_in";
    }

    @Override
    public void execute(Long inspectionId,
                        InspectionJob inspectionJob) {

        String ruleParamJson = inspectionJob.getRuleParamJson();
        JSONObject jsonObject = JSON.parseObject(ruleParamJson);
        Integer threshold = jsonObject.getInteger("threshold");
        List<ApiFanInDTO> list =
                mapper.findFanIn(1, threshold);

        for (ApiFanInDTO dto : list) {
            InspectionIssue issue = InspectionIssue.builder()
                    .inspectionId(inspectionId)
                    .jobId(inspectionJob.getJobId())
                    .objectId(dto.getApiId())
                    .severity("HIGH")
                    .description("API dependency too high: "
                            + dto.getDepCount())
                            .build();
            issueMapper.insert(issue);
        }
    }
}
