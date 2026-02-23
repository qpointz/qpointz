package io.qpointz.mill.metadata.configuration;

import io.qpointz.mill.metadata.service.FacetContentValidator;
import io.qpointz.mill.metadata.service.JsonSchemaFacetContentValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@ConditionalOnClass(name = "com.networknt.schema.JsonSchemaFactory")
public class JsonSchemaValidatorConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public FacetContentValidator facetContentValidator() {
        log.info("JSON Schema validator library detected, enabling content validation");
        return new JsonSchemaFacetContentValidator();
    }
}
