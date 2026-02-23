package io.qpointz.mill.metadata.configuration

import io.qpointz.mill.metadata.service.FacetContentValidator
import io.qpointz.mill.metadata.service.JsonSchemaFacetContentValidator
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/** Enables JSON-schema facet validation when networknt schema library is present. */
@Configuration
@ConditionalOnClass(name = ["com.networknt.schema.JsonSchemaFactory"])
open class JsonSchemaValidatorConfiguration {

    @Bean
    @ConditionalOnMissingBean
    open fun facetContentValidator(): FacetContentValidator {
        log.info("JSON Schema validator library detected, enabling content validation")
        return JsonSchemaFacetContentValidator()
    }

    companion object {
        private val log = LoggerFactory.getLogger(JsonSchemaValidatorConfiguration::class.java)
    }
}
