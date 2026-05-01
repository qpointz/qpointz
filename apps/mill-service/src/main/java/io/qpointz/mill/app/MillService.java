package io.qpointz.mill.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Spring Boot entry point for the Mill HTTP and batch runtime.
 *
 * <p>Component scanning covers the {@code io.qpointz} namespace; JPA entity scanning uses the same
 * base so persistence modules can register entities without extra configuration.
 */
@SpringBootApplication(scanBasePackages = "io.qpointz")
@EntityScan(basePackages = "io.qpointz")
@EnableScheduling
public class MillService {

    /**
     * Boots the application using default Spring Boot configuration discovery.
     *
     * @param args standard Spring Boot command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(new Class[] {MillService.class}, args);
    }

}
