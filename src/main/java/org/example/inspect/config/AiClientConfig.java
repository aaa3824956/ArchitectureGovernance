package org.example.inspect.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;

@Configuration
@EnableConfigurationProperties(AiProperties.class)
public class AiClientConfig {

    @Bean(name = "aiRestClientBuilder")
    public RestClient.Builder aiRestClientBuilder(AiProperties properties) {
        return RestClient.builder()
                .requestFactory(buildRequestFactory(properties))
                .baseUrl(trimTrailingSlash(properties.getBaseUrl()));
    }

    private static ClientHttpRequestFactory buildRequestFactory(AiProperties p) {
        Duration connect = Duration.ofMillis(p.getConnectTimeoutMs());
        Duration read = Duration.ofMillis(p.getReadTimeoutMs());
        String host = p.getProxyHost();
        boolean proxyOn = host != null && !host.isBlank() && p.getProxyPort() > 0;
        // #region agent log
        agentLog("AiClientConfig.buildRequestFactory", "H1_proxy_factory",
                Map.of("proxyEnabled", proxyOn, "connectTimeoutMs", p.getConnectTimeoutMs()));
        // #endregion
        if (proxyOn) {
            String proxyHost = Objects.requireNonNull(host).trim();
            HttpClient httpClient = HttpClient.newBuilder()
                    .connectTimeout(connect)
                    .proxy(ProxySelector.of(new InetSocketAddress(proxyHost, p.getProxyPort())))
                    .build();
            // #region agent log
            agentLog("AiClientConfig.buildRequestFactory", "H1_jdk_factory", Map.of("ok", true));
            // #endregion
            JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
            factory.setReadTimeout(read);
            return factory;
        }
        SimpleClientHttpRequestFactory simple = new SimpleClientHttpRequestFactory();
        simple.setConnectTimeout(connect);
        simple.setReadTimeout(read);
        // #region agent log
        agentLog("AiClientConfig.buildRequestFactory", "H1_simple_factory", Map.of("ok", true));
        // #endregion
        return simple;
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

    private static String trimTrailingSlash(String url) {
        if (url == null || url.isEmpty()) {
            return url;
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
