package io.qpointz.mill.service.configuration;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Splits SpringDoc into a small well-known group and the main API group so {@code /.well-known/mill}
 * does not overwhelm the primary OpenAPI document.
 */
@Configuration
public class OpenApiConfiguration {

    /**
     * OpenAPI slice for Mill discovery metadata only.
     *
     * @return grouped API limited to {@code /.well-known/mill}
     */
    @Bean
    public GroupedOpenApi wellKnownApi() {
        return GroupedOpenApi.builder()
                .group("well-known")
                .pathsToMatch("/.well-known/mill")
                .build();
    }

    /**
     * Default OpenAPI slice for the rest of the HTTP surface (excludes well-known paths).
     *
     * @return grouped API matching all paths except {@code /.well-known/**}
     */
    @Bean
    public GroupedOpenApi mainApi() {
        return GroupedOpenApi.builder()
                .group("api")
                .pathsToMatch("/**")
                .pathsToExclude("/.well-known/**")
                .build();
    }

}
