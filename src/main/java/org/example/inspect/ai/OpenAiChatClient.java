package org.example.inspect.ai;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;

import org.example.inspect.config.AiProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class OpenAiChatClient {

    private final RestClient.Builder restClientBuilder;
    private final AiProperties properties;

    public OpenAiChatClient(
            @Qualifier("aiRestClientBuilder") RestClient.Builder aiRestClientBuilder,
            AiProperties properties) {
        this.restClientBuilder = aiRestClientBuilder;
        this.properties = properties;
    }

    public String chatCompletions(String requestBodyJson) {
        String path = normalizePath(properties.getChatPath());
        try {
            return restClientBuilder
                    .build()
                    .post()
                    .uri(path)
                    .header("Authorization", "Bearer " + properties.getApiKey())
                    .header("Content-Type", "application/json")
                    .body(requestBodyJson)
                    .retrieve()
                    .body(String.class);
        } catch (Exception e) {
            // #region agent log
            String msg = e.getMessage() != null ? e.getMessage() : "";
            if (msg.length() > 240) {
                msg = msg.substring(0, 240);
            }
            agentLog("OpenAiChatClient.chatCompletions", "H2_http_error",
                    Map.of("ex", e.getClass().getSimpleName(), "msg", msg));
            // #endregion
            throw e;
        }
    }

    private static void agentLog(String location, String hypothesisId, Map<String, ?> data) {
        try {
            StringBuilder sb = new StringBuilder(256);
            sb.append("{\"sessionId\":\"a2a378\",\"hypothesisId\":\"").append(hypothesisId)
                    .append("\",\"location\":\"").append(location)
                    .append("\",\"message\":\"debug\",\"timestamp\":").append(System.currentTimeMillis())
                    .append(",\"data\":{");
            boolean first = true;
            for (Map.Entry<String, ?> e : data.entrySet()) {
                if (!first) {
                    sb.append(',');
                }
                first = false;
                sb.append('"').append(e.getKey()).append("\":");
                Object v = e.getValue();
                if (v instanceof String s) {
                    sb.append('"').append(s.replace("\\", "\\\\").replace("\"", "\\\"")).append('"');
                } else {
                    sb.append(v);
                }
            }
            sb.append("}}\n");
            Files.writeString(Path.of("debug-a2a378.log"), sb.toString(), StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (Exception ignored) {
            // debug ingest only
        }
    }

    private static String normalizePath(String p) {
        if (p == null || p.isEmpty()) {
            return "/chat/completions";
        }
        return p.startsWith("/") ? p : "/" + p;
    }
}
