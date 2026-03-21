package io.qpointz.mill.security.auth.configuration

import io.qpointz.mill.security.auth.controllers.AuthController
import io.qpointz.mill.security.auth.controllers.AuthPublicController
import io.qpointz.mill.security.domain.UserIdentityResolutionService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.security.authentication.AuthenticationManager

/**
 * Spring Boot autoconfiguration entry point for the auth service.
 *
 * Registers both controllers and both security filter chain configurations.
 * The [AuthenticationManager] and [UserIdentityResolutionService] are injected as
 * optional so that the module works correctly in security-off mode. The `securityEnabled`
 * flag (from `mill.security.enable`) is passed to controllers so they can return
 * appropriate anonymous responses when security is disabled, regardless of whether
 * other security-related beans happen to be present on the classpath.
 */
@AutoConfiguration
@Import(
    AuthPublicSecurityConfiguration::class,
    AuthSecuredSecurityConfiguration::class,
)
open class AuthServiceConfiguration {

    /**
     * Provides the [AuthPublicController] bean.
     *
     * @param authenticationManager optional — absent when security is disabled
     * @param identityResolutionService optional — absent when security is disabled
     * @param securityEnabled whether `mill.security.enable` is true; defaults to `false`
     * @return configured [AuthPublicController]
     */
    @Bean
    open fun authPublicController(
        @Autowired(required = false) authenticationManager: AuthenticationManager?,
        @Autowired(required = false) identityResolutionService: UserIdentityResolutionService?,
        @Value("\${mill.security.enable:false}") securityEnabled: Boolean,
    ): AuthPublicController = AuthPublicController(authenticationManager, identityResolutionService, securityEnabled)

    /**
     * Provides the [AuthController] bean.
     *
     * @param identityResolutionService optional — absent when security is disabled
     * @param securityEnabled whether `mill.security.enable` is true; defaults to `false`
     * @return configured [AuthController]
     */
    @Bean
    open fun authController(
        @Autowired(required = false) identityResolutionService: UserIdentityResolutionService?,
        @Value("\${mill.security.enable:false}") securityEnabled: Boolean,
    ): AuthController = AuthController(identityResolutionService, securityEnabled)
}
