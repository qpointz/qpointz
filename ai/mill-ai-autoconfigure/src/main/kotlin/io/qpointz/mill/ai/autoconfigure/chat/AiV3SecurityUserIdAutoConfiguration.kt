package io.qpointz.mill.ai.autoconfigure.chat

import io.qpointz.mill.ai.chat.PropertiesUserIdResolver
import io.qpointz.mill.ai.chat.UserIdResolver
import io.qpointz.mill.security.domain.UserIdentityResolutionService
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.context.annotation.Bean
import io.qpointz.mill.ai.autoconfigure.AiV3AutoConfiguration

/**
 * Registers [SecurityUserIdResolver] when Mill auth identity resolution is available.
 */
@AutoConfiguration
@AutoConfigureBefore(AiV3AutoConfiguration::class)
@ConditionalOnClass(UserIdentityResolutionService::class)
@ConditionalOnBean(UserIdentityResolutionService::class)
class AiV3SecurityUserIdAutoConfiguration {

    /**
     * Security-aware [UserIdResolver] for multi-user chat ownership (WI-318).
     */
    @Bean
    @ConditionalOnMissingBean(UserIdResolver::class)
    fun securityUserIdResolver(
        identityResolutionService: UserIdentityResolutionService,
        @Value("\${mill.security.enable:false}") securityEnabled: Boolean,
        props: AiV3ChatProperties,
    ): UserIdResolver = SecurityUserIdResolver(
        identityResolutionService = identityResolutionService,
        securityEnabled = securityEnabled,
        fallback = PropertiesUserIdResolver(props.defaultUserId),
    )
}
