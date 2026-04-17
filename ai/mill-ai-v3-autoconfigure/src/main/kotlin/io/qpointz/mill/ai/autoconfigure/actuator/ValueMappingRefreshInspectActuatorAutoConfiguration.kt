package io.qpointz.mill.ai.autoconfigure.actuator

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean

/**
 * Registers [ValueMappingRefreshInspectEndpoint] when Spring Boot Actuator is on the classpath.
 */
@AutoConfiguration
@ConditionalOnClass(name = ["org.springframework.boot.actuate.endpoint.annotation.Endpoint"])
class ValueMappingRefreshInspectActuatorAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(ValueMappingRefreshInspectEndpoint::class)
    fun valuemapInspectEndpoint(
        context: ApplicationContext,
    ): ValueMappingRefreshInspectEndpoint = ValueMappingRefreshInspectEndpoint(context)
}
