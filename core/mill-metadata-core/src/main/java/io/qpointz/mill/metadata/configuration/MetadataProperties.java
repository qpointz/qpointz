package io.qpointz.mill.metadata.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for faceted metadata service (v2).
 * Uses separate config section to avoid conflicts with legacy metadata config.
 */
@Data
@ConfigurationProperties(prefix = "mill.metadata.v2")
public class MetadataProperties {
    
    private Storage storage = new Storage();
    private File file = new File();
    
    @Data
    public static class Storage {
        private String type = "file";  // file, jpa, composite, external
    }
    
    @Data
    public static class File {
        private String path = "classpath:metadata/example.yml";
        private boolean watch = false;  // Auto-reload on changes
    }
}

