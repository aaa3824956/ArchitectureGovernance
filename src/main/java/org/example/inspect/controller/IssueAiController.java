package org.example.inspect.controller;

import org.example.inspect.dto.AiSuggestRequest;
import org.example.inspect.dto.AiSuggestionView;
import org.example.inspect.service.AiIssueAssistantService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/inspection/issues")
public class IssueAiController {

    private final AiIssueAssistantService aiIssueAssistantService;

    public IssueAiController(AiIssueAssistantService aiIssueAssistantService) {
        this.aiIssueAssistantService = aiIssueAssistantService;
    }

    /**
     * 对单条巡检问题生成「解读 + 修复建议」结构化结果并落库。
     */
    @PostMapping("/{issueId}/ai/suggest")
    public ResponseEntity<AiSuggestionView> suggest(
            @PathVariable Long issueId,
            @RequestBody(required = false) AiSuggestRequest body) {
        String note = body != null ? body.getUserNote() : null;
        AiSuggestionView view = aiIssueAssistantService.suggestExplainFix(issueId, note);
        return ResponseEntity.ok(view);
    }

    /**
     * 查询某 issue 最新一条 AI 建议（可按 kind 过滤，默认任意 kind 取最新）。
     */
    @GetMapping("/{issueId}/ai/latest")
    public ResponseEntity<AiSuggestionView> latest(
            @PathVariable Long issueId,
            @RequestParam(required = false) String kind) {
        AiSuggestionView view = aiIssueAssistantService.latest(issueId, kind);
        if (view == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(view);
    }
}
