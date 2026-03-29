package org.example.inspect.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "inspection.ai")
public class AiProperties {

    /** 关闭时不调用外网模型，接口返回 503（demo-mode 为 true 时不受此限制） */
    private boolean enabled = true;

    /**
     * 本地演示：不调大模型，根据事实 JSON 生成固定结构的示例输出，无需 api-key。
     * 适合答辩 / 录屏 / 无密钥环境验收接口与落库。
     */
    private boolean demoMode = true;

    /** OpenAI 兼容接口根路径；默认硅基流动国内站 */
    private String baseUrl = "https://api.siliconflow.cn/v1";

    /** 相对 baseUrl 的路径 */
    private String chatPath = "/chat/completions";

    private String apiKey = "";

    private String model = "Qwen/Qwen2.5-7B-Instruct";

    private double temperature = 0.3;

    private long connectTimeoutMs = 20_000L;

    private long readTimeoutMs = 180_000L;

    /** 是否请求 response_format=json_object（部分自建网关不支持时可关） */
    private boolean jsonObjectResponseFormat = true;

    /**
     * 访问境外 API 时本地 HTTP 代理主机（如 127.0.0.1）；留空则直连（硅基流动 .cn 通常不需要）。
     * 常见：Clash / V2Ray 的 HTTP 端口（如 7890）。
     */
    private String proxyHost = "";

    /** 与 proxyHost 成对使用；为 0 或未配 host 时不走代理 */
    private int proxyPort = 0;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public boolean isDemoMode() { return demoMode; }
    public void setDemoMode(boolean demoMode) { this.demoMode = demoMode; }

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

    public String getChatPath() { return chatPath; }
    public void setChatPath(String chatPath) { this.chatPath = chatPath; }

    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public double getTemperature() { return temperature; }
    public void setTemperature(double temperature) { this.temperature = temperature; }

    public long getConnectTimeoutMs() { return connectTimeoutMs; }
    public void setConnectTimeoutMs(long connectTimeoutMs) { this.connectTimeoutMs = connectTimeoutMs; }

    public long getReadTimeoutMs() { return readTimeoutMs; }
    public void setReadTimeoutMs(long readTimeoutMs) { this.readTimeoutMs = readTimeoutMs; }

    public boolean isJsonObjectResponseFormat() { return jsonObjectResponseFormat; }
    public void setJsonObjectResponseFormat(boolean jsonObjectResponseFormat) {
        this.jsonObjectResponseFormat = jsonObjectResponseFormat;
    }

    public String getProxyHost() { return proxyHost; }
    public void setProxyHost(String proxyHost) { this.proxyHost = proxyHost; }

    public int getProxyPort() { return proxyPort; }
    public void setProxyPort(int proxyPort) { this.proxyPort = proxyPort; }
}
