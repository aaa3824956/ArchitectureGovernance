package org.example.inspect.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.inspect.ai.OpenAiChatClient;
import org.example.inspect.common.Sha256Hex;
import org.example.inspect.config.AiProperties;
import org.example.inspect.dto.AiSuggestionView;
import org.example.inspect.entity.AiSuggestion;
import org.example.inspect.entity.InspectionIssue;
import org.example.inspect.entity.InspectionJob;
import org.example.inspect.repository.AiSuggestionMapper;
import org.example.inspect.repository.InspectionIssueMapper;
import org.example.inspect.repository.InspectionJobMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AiIssueAssistantService {

    public static final String KIND_EXPLAIN_FIX = "EXPLAIN_FIX";

    private static final String SYSTEM_PROMPT = """
            你是企业架构治理助手。用户会提供一条「架构巡检问题」的结构化事实 JSON。
            你必须严格遵守：
            1) 只根据事实 JSON 与用户补充说明作答；事实中没有的字段一律写「未知」，禁止编造 API 名称、调用次数、路径或微服务名。
            2) 区分事实与推测：推测必须明确使用「可能」等措辞。
            3) 输出必须是单个 JSON 对象（不要 Markdown 代码块），且包含以下键：
               - summary: string，一句话结论
               - why_it_matters: string[]，为何重要（2～5 条）
               - likely_causes: string[]，可能原因（可含不确定性说明）
               - recommended_actions: array of { "step": number, "action": string, "owner_hint": string }
               - questions_for_humans: string[]，需要人工确认的问题
               - confidence: "high"|"medium"|"low"
            4) 使用简体中文。
            """;

    private final AiProperties aiProperties;
    private final OpenAiChatClient chatClient;
    private final InspectionIssueMapper issueMapper;
    private final InspectionJobMapper jobMapper;
    private final AiSuggestionMapper aiSuggestionMapper;

    public AiIssueAssistantService(
            AiProperties aiProperties,
            OpenAiChatClient chatClient,
            InspectionIssueMapper issueMapper,
            InspectionJobMapper jobMapper,
            AiSuggestionMapper aiSuggestionMapper) {
        this.aiProperties = aiProperties;
        this.chatClient = chatClient;
        this.issueMapper = issueMapper;
        this.jobMapper = jobMapper;
        this.aiSuggestionMapper = aiSuggestionMapper;
    }

    public AiSuggestionView suggestExplainFix(Long issueId, String userNote) {
        InspectionIssue issue = issueMapper.selectById(issueId);
        if (issue == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "issue 不存在: " + issueId);
        }

        InspectionJob job = null;
        if (issue.getJobId() != null) {
            job = jobMapper.selectById(issue.getJobId());
        }

        JSONObject facts = buildFacts(issue, job);
        String factsJson = facts.toJSONString();
        String userContent = buildUserPayload(factsJson, userNote);
        String promptForHash = SYSTEM_PROMPT + "\n---\n" + userContent;
        String promptHash = Sha256Hex.ofUtf8(promptForHash);
        String factsHash = Sha256Hex.ofUtf8(factsJson);

        if (aiProperties.isDemoMode()) {
            return persistAndReturn(
                    issueId,
                    promptHash,
                    factsHash,
                    buildDemoOutputJson(facts, userNote),
                    buildDemoOutputJson(facts, userNote),
                    "demo-local",
                    1L);
        }

        if (!aiProperties.isEnabled()) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "AI 未启用：请设置 inspection.ai.enabled=true 并配置 api-key，或开启 inspection.ai.demo-mode=true 做本地演示");
        }
        if (aiProperties.getApiKey() == null || aiProperties.getApiKey().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "未配置 inspection.ai.api-key（演示可开启 inspection.ai.demo-mode=true）");
        }

        JSONObject request = new JSONObject();
        request.put("model", aiProperties.getModel());
        request.put("temperature", aiProperties.getTemperature());
        JSONArray messages = new JSONArray();
        JSONObject sysMsg = new JSONObject();
        sysMsg.put("role", "system");
        sysMsg.put("content", SYSTEM_PROMPT);
        messages.add(sysMsg);
        JSONObject userMsg = new JSONObject();
        userMsg.put("role", "user");
        userMsg.put("content", userContent);
        messages.add(userMsg);
        request.put("messages", messages);
        if (aiProperties.isJsonObjectResponseFormat()) {
            JSONObject fmt = new JSONObject();
            fmt.put("type", "json_object");
            request.put("response_format", fmt);
        }

        long t0 = System.currentTimeMillis();
        String httpResponse;
        try {
            httpResponse = chatClient.chatCompletions(request.toJSONString());
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "调用大模型失败: " + e.getMessage());
        }
        long duration = System.currentTimeMillis() - t0;

        String assistantRaw;
        try {
            assistantRaw = extractAssistantContent(httpResponse);
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "解析模型响应失败: " + e.getMessage());
        }
        String outputJson = normalizeToJsonObjectString(assistantRaw);

        return persistAndReturn(
                issueId,
                promptHash,
                factsHash,
                outputJson,
                assistantRaw,
                aiProperties.getModel(),
                duration);
    }

    private AiSuggestionView persistAndReturn(
            Long issueId,
            String promptHash,
            String factsHash,
            String outputJson,
            String rawAssistantText,
            String model,
            long durationMs) {
        AiSuggestion row = new AiSuggestion();
        row.setIssueId(issueId);
        row.setKind(KIND_EXPLAIN_FIX);
        row.setModel(model);
        row.setPromptHash(promptHash);
        row.setInputFactsHash(factsHash);
        row.setOutputJson(outputJson);
        row.setRawAssistantText(truncate(rawAssistantText, 65_000));
        row.setDurationMs(durationMs);
        aiSuggestionMapper.insert(row);
        return toView(row);
    }

    /**
     * 演示用结构化输出：字段与真实大模型约定一致，内容根据 facts 拼接，不编造库外数据。
     */
    private static String buildDemoOutputJson(JSONObject facts, String userNote) {
        String desc = facts.getString("description");
        if (desc == null) {
            desc = "（无描述）";
        }
        String sev = facts.getString("severity");
        String objType = facts.getString("objectType");
        Object objIdObj = facts.get("objectId");
        String jobType = facts.getString("jobType");

        JSONObject out = new JSONObject(true);
        out.put("summary", "【演示模式】根据巡检事实，该问题需要关注治理优先级（严重程度: "
                + (sev != null ? sev : "未知") + "）。");
        JSONArray why = new JSONArray();
        why.add("扇入过高的接口往往成为变更涟漪中心，一次修改可能影响多个上游调用方。");
        why.add("事实描述已包含调用规模线索，建议结合发布节奏评估是否拆分读模型或引入防腐层。");
        if (jobType != null && !jobType.isEmpty()) {
            why.add("当前规则类型为 " + jobType + "，可与团队约定的接口分层/版本策略对齐检查。");
        }
        out.put("why_it_matters", why);

        JSONArray causes = new JSONArray();
        causes.add("可能存在历史复用导致同一接口承担过多职责（仅为可能，需结合代码与领域边界确认）。");
        causes.add("可能缺少面向查询场景的拆分或聚合接口（仅为可能）。");
        out.put("likely_causes", causes);

        JSONArray actions = new JSONArray();
        JSONObject a1 = new JSONObject(true);
        a1.put("step", 1);
        a1.put("action", "梳理调用方清单与核心场景，确认是否可用「读模型 / 查询服务」分流。");
        a1.put("owner_hint", "架构 + 业务负责人");
        actions.add(a1);
        JSONObject a2 = new JSONObject(true);
        a2.put("step", 2);
        a2.put("action", "若短期无法拆分，至少补充契约测试或接口稳定性基线，降低回归风险。");
        a2.put("owner_hint", "研发负责人");
        actions.add(a2);
        out.put("recommended_actions", actions);

        JSONArray questions = new JSONArray();
        questions.add("该接口是否被多个业务域共用？是否存在「顺手调用」导致的隐性耦合？");
        questions.add("ruleParamJson 中的阈值是否仍符合当前团队治理标准？");
        out.put("questions_for_humans", questions);

        if (userNote != null && !userNote.isBlank()) {
            questions.add("用户补充说明已收到，是否在下次迭代纳入专项评审？附注：" + userNote.trim());
        }

        out.put("confidence", "medium");
        out.put("_demo", true);
        out.put("_facts_echo", desc);
        out.put("_object", (objType != null ? objType : "?") + ":" + (objIdObj != null ? String.valueOf(objIdObj) : "?"));
        return out.toJSONString();
    }

    public AiSuggestionView latest(Long issueId, String kind) {
        LambdaQueryWrapper<AiSuggestion> q = new LambdaQueryWrapper<AiSuggestion>()
                .eq(AiSuggestion::getIssueId, issueId);
        if (kind != null && !kind.isBlank()) {
            q.eq(AiSuggestion::getKind, kind);
        }
        q.orderByDesc(AiSuggestion::getCreatedAt).last("LIMIT 1");
        AiSuggestion one = aiSuggestionMapper.selectOne(q);
        if (one == null) {
            return null;
        }
        return toView(one);
    }

    private static JSONObject buildFacts(InspectionIssue issue, InspectionJob job) {
        JSONObject f = new JSONObject(true);
        f.put("issueId", issue.getIssueId());
        f.put("inspectionId", issue.getInspectionId());
        f.put("jobId", issue.getJobId());
        f.put("objectType", issue.getObjectType());
        f.put("objectId", issue.getObjectId());
        f.put("severity", issue.getSeverity());
        f.put("description", issue.getDescription());
        f.put("status", issue.getStatus());
        f.put("fingerprint", issue.getFingerprint());
        if (job != null) {
            f.put("jobType", job.getJobType());
            f.put("ruleParamJson", job.getRuleParamJson());
            f.put("jobSeverity", job.getSeverity());
        } else {
            f.put("jobType", null);
            f.put("ruleParamJson", null);
            f.put("jobSeverity", null);
        }
        return f;
    }

    private static String buildUserPayload(String factsJson, String userNote) {
        String note = (userNote == null || userNote.isBlank()) ? "（无）" : userNote.trim();
        return """
                以下是巡检系统提供的结构化事实（JSON）。请仅基于这些事实与下面的用户补充生成回答。
                
                事实 JSON：
                %s
                
                用户补充说明：
                %s
                """.formatted(factsJson, note);
    }

    private static String extractAssistantContent(String httpResponseBody) {
        JSONObject root = JSON.parseObject(httpResponseBody);
        if (root == null) {
            throw new IllegalArgumentException("响应体不是合法 JSON");
        }
        JSONArray choices = root.getJSONArray("choices");
        if (choices == null || choices.isEmpty()) {
            throw new IllegalArgumentException("响应缺少 choices");
        }
        JSONObject message = choices.getJSONObject(0).getJSONObject("message");
        if (message == null) {
            throw new IllegalArgumentException("响应缺少 message");
        }
        String content = message.getString("content");
        if (content == null) {
            throw new IllegalArgumentException("响应缺少 content");
        }
        return content;
    }

    private static String normalizeToJsonObjectString(String assistantContent) {
        String trimmed = assistantContent == null ? "" : assistantContent.trim();
        trimmed = stripMarkdownJsonFence(trimmed);
        try {
            JSONObject o = JSON.parseObject(trimmed);
            if (o == null) {
                throw new IllegalArgumentException("empty json");
            }
            return o.toJSONString();
        } catch (Exception e) {
            JSONObject err = new JSONObject();
            err.put("parse_error", true);
            err.put("summary", "模型返回非合法 JSON，已原样截断保存");
            err.put("raw_excerpt", truncate(trimmed, 2_000));
            return err.toJSONString();
        }
    }

    private static String stripMarkdownJsonFence(String text) {
        if (!text.startsWith("```")) {
            return text;
        }
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        return text;
    }

    private static String truncate(String s, int max) {
        if (s == null) {
            return null;
        }
        return s.length() <= max ? s : s.substring(0, max);
    }

    private static AiSuggestionView toView(AiSuggestion row) {
        AiSuggestionView v = new AiSuggestionView();
        v.setId(row.getId());
        v.setIssueId(row.getIssueId());
        v.setKind(row.getKind());
        v.setModel(row.getModel());
        v.setOutputJson(row.getOutputJson());
        v.setDurationMs(row.getDurationMs());
        v.setCreatedAt(row.getCreatedAt());
        return v;
    }
}
