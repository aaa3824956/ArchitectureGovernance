package org.example.inspect.engine;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.example.inspect.dto.ApiFanInDTO;
import org.example.inspect.entity.InspectionIssue;
import org.example.inspect.entity.InspectionJob;
import org.example.inspect.repository.ApiDependencyMapper;
import org.example.inspect.repository.InspectionIssueMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

@Component
public class ApiFanInJobExecutor implements JobExecutor {

    private static final Logger log = LoggerFactory.getLogger(ApiFanInJobExecutor.class);

    private static final String RULE_TYPE = "api_fan_in";
    private static final int DEFAULT_THRESHOLD = 10;

    @Autowired
    private ApiDependencyMapper apiDependencyMapper;

    @Autowired
    private InspectionIssueMapper issueMapper;

    @Override
    public String getRuleType() {
        return RULE_TYPE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void execute(Long inspectionId, InspectionJob inspectionJob) {
        Long jobId = inspectionJob.getJobId();
        RuleParam param = parseParam(inspectionJob.getRuleParamJson());

        log.info("[api_fan_in] 开始执行: inspectionId={}, jobId={}, releaseUnitId={}, threshold={}",
                inspectionId, jobId, param.releaseUnitId, param.threshold);

        Set<String> activeFingerprints = new HashSet<>(
                issueMapper.findActiveFingerprintsByJobId(jobId));
        Set<String> closedFingerprints = new HashSet<>(
                issueMapper.findClosedFingerprintsByJobId(jobId));

        issueMapper.deleteActiveByJobId(jobId);

        List<ApiFanInDTO> violations = apiDependencyMapper.findFanIn(
                param.releaseUnitId, param.threshold);

        int created = 0;
        for (ApiFanInDTO dto : violations) {
            String fingerprint = buildFingerprint(RULE_TYPE, dto.getApiId(), param.releaseUnitId);

            if (closedFingerprints.contains(fingerprint)) {
                continue;
            }

            String status = activeFingerprints.contains(fingerprint) ? "OPEN" : "NEW";

            String apiLabel = dto.getApiName() != null
                    ? dto.getApiName() + "(id=" + dto.getApiId() + ")"
                    : "apiId=" + dto.getApiId();

            String description = String.format(
                    "接口 %s 扇入过高: 被 %d 个外部接口调用（阈值 %d），调用方微服务: [%s]",
                    apiLabel,
                    dto.getDepCount(),
                    param.threshold,
                    dto.getCallerUnitIds() != null ? dto.getCallerUnitIds() : "N/A");

            InspectionIssue issue = new InspectionIssue();
            issue.setInspectionId(inspectionId);
            issue.setJobId(jobId);
            issue.setObjectType("API");
            issue.setObjectId(dto.getApiId());
            issue.setSeverity(inspectionJob.getSeverity());
            issue.setDescription(description);
            issue.setFingerprint(fingerprint);
            issue.setStatus(status);

            issueMapper.insert(issue);
            created++;
        }

        log.info("[api_fan_in] 执行完成: inspectionId={}, jobId={}, 发现问题数={}",
                inspectionId, jobId, created);
    }

    private RuleParam parseParam(String ruleParamJson) {
        RuleParam param = new RuleParam();
        if (ruleParamJson == null || ruleParamJson.isBlank()) {
            log.warn("[api_fan_in] ruleParamJson 为空，使用默认参数");
            return param;
        }
        JSONObject json = JSON.parseObject(ruleParamJson);
        if (json.containsKey("threshold")) {
            param.threshold = json.getInteger("threshold");
        }
        if (json.containsKey("releaseUnitId")) {
            param.releaseUnitId = json.getInteger("releaseUnitId");
        }
        return param;
    }

    private static String buildFingerprint(String ruleType, Long objectId, Integer releaseUnitId) {
        String raw = ruleType + ":" + releaseUnitId + ":" + objectId;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(64);
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return raw;
        }
    }

    private static class RuleParam {
        int threshold = DEFAULT_THRESHOLD;
        int releaseUnitId = 1;
    }
}
