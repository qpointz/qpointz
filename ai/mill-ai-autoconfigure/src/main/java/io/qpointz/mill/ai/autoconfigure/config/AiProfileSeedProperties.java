package io.qpointz.mill.ai.autoconfigure.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Ordered agent profile seed resources ({@code mill.ai.profiles.seed}).
 *
 * <p>Each location is a Spring {@link org.springframework.core.io.Resource} URI
 * (e.g. {@code classpath:profiles/platform-agent-profiles.yaml}, {@code file:…})
 * resolved via {@link org.springframework.core.io.ResourceLoader}.
 */
@ConfigurationProperties(prefix = "mill.ai.profiles.seed")
public class AiProfileSeedProperties {

    private List<String> resources = new ArrayList<>(List.of("classpath:profiles/platform-agent-profiles.yaml"));

    /**
     * @return ordered Spring resource locations
     */
    public List<String> getResources() {
        return resources;
    }

    /**
     * @param resources replacement list; {@code null} clears to empty
     */
    public void setResources(List<String> resources) {
        this.resources = resources != null ? resources : new ArrayList<>();
    }
}
