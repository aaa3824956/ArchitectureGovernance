package org.example.inspect.controller;

import org.example.inspect.config.AiProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/inspection/ai")
public class AiStatusController {

    private final AiProperties aiProperties;

    public AiStatusController(AiProperties aiProperties) {
        this.aiProperties = aiProperties;
    }

    /** 演示/排障：当前 AI 开关与模式 */
    @GetMapping("/status")
    public Map<String, Object> status() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("enabled", aiProperties.isEnabled());
        m.put("demoMode", aiProperties.isDemoMode());
        m.put("model", aiProperties.getModel());
        m.put("baseUrl", aiProperties.getBaseUrl());
        String key = aiProperties.getApiKey();
        m.put("apiKeyConfigured", key != null && !key.isBlank());
        String ph = aiProperties.getProxyHost();
        m.put("proxyConfigured", ph != null && !ph.isBlank() && aiProperties.getProxyPort() > 0);
        return m;
    }
}
