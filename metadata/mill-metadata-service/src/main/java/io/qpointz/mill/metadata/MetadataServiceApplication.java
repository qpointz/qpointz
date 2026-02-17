package io.qpointz.mill.metadata;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot application for metadata service.
 */
@SpringBootApplication(scanBasePackages = {
    "io.qpointz.mill.metadata",
    "io.qpointz.mill.services"
})
public class MetadataServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(MetadataServiceApplication.class, args);
    }
}

