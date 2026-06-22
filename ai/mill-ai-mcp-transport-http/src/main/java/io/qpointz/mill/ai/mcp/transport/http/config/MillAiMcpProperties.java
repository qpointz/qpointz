package io.qpointz.mill.ai.mcp.transport.http.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * {@code mill.ai.mcp.*} settings for the HTTP MCP servlet transport.
 */
@ConfigurationProperties(prefix = "mill.ai.mcp")
public class MillAiMcpProperties {

    private boolean enabled = false;

    private String profile = "hello-world";

    private List<String> capabilities = new ArrayList<>();

    private final Http http = new Http();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile != null ? profile : "hello-world";
    }

    public List<String> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(List<String> capabilities) {
        this.capabilities = capabilities != null ? capabilities : new ArrayList<>();
    }

    public Http getHttp() {
        return http;
    }

    /**
     * HTTP transport settings.
     */
    public static class Http {

        private String endpoint = "/services/mcp";

        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint != null && !endpoint.isBlank() ? endpoint : "/services/mcp";
        }
    }
}
