package io.qpointz.mill.metadata.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI/Swagger configuration.
 */
@Configuration
public class OpenApiConfig {
    
    @Bean
    public OpenAPI metadataServiceOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Mill Metadata Service API")
                .description("REST API for faceted metadata management with scope support")
                .version("v1")
                .contact(new Contact()
                    .name("Mill Team")
                    .email("support@qpointz.io"))
                .license(new License()
                    .name("Proprietary")
                    .url("https://qpointz.io")));
    }
}

