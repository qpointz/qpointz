package io.qpointz.mill.persistence.security.jpa.configuration

import io.qpointz.mill.persistence.security.jpa.audit.JpaAuthAuditService
import io.qpointz.mill.persistence.security.jpa.repositories.AuthEventRepository
import io.qpointz.mill.persistence.security.jpa.repositories.GroupMembershipRepository
import io.qpointz.mill.persistence.security.jpa.repositories.UserIdentityRepository
import io.qpointz.mill.persistence.security.jpa.repositories.UserProfileRepository
import io.qpointz.mill.persistence.security.jpa.repositories.UserRepository
import io.qpointz.mill.persistence.security.jpa.service.JpaUserIdentityResolutionService
import io.qpointz.mill.security.audit.AuthAuditService
import io.qpointz.mill.security.domain.UserIdentityResolutionService
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.persistence.autoconfigure.EntityScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

/**
 * Spring Boot autoconfiguration for the security JPA persistence module.
 *
 * Registers the `io.qpointz.mill.persistence.security.jpa` entity scan package
 * and enables JPA repositories in the same package hierarchy. Exposes
 * [JpaUserIdentityResolutionService] as a [UserIdentityResolutionService] bean.
 *
 * Also imports [JpaPasswordAuthenticationConfiguration], which wires JPA-backed
 * basic authentication into the Spring Security chain when security is enabled.
 *
 * Conditional on [io.qpointz.mill.persistence.security.jpa.entities.UserRecord]
 * being present on the classpath, which is guaranteed when this module is a dependency.
 */
@AutoConfiguration
@ConditionalOnClass(name = ["io.qpointz.mill.persistence.security.jpa.entities.UserRecord"])
@EntityScan(basePackages = ["io.qpointz.mill.persistence.security.jpa.entities"])
@EnableJpaRepositories(basePackages = ["io.qpointz.mill.persistence.security.jpa.repositories"])
@Import(JpaPasswordAuthenticationConfiguration::class)
class SecurityJpaConfiguration {

    /**
     * Exposes [JpaAuthAuditService] as the [AuthAuditService] bean.
     *
     * @param repo repository for persisting [io.qpointz.mill.persistence.security.jpa.entities.AuthEventRecord] rows
     * @return [AuthAuditService] backed by JPA
     */
    @Bean
    fun authAuditService(repo: AuthEventRepository): AuthAuditService =
        JpaAuthAuditService(repo)

    /**
     * Exposes [JpaUserIdentityResolutionService] as the [UserIdentityResolutionService] bean.
     *
     * @param userRepo repository for canonical user records
     * @param identityRepo repository for provider/subject → userId mappings
     * @param profileRepo repository for user profile records
     * @param membershipRepo repository for group membership resolution
     * @return [UserIdentityResolutionService] implementation backed by JPA
     */
    @Bean
    fun userIdentityResolutionService(
        userRepo: UserRepository,
        identityRepo: UserIdentityRepository,
        profileRepo: UserProfileRepository,
        membershipRepo: GroupMembershipRepository,
    ): UserIdentityResolutionService =
        JpaUserIdentityResolutionService(userRepo, identityRepo, profileRepo, membershipRepo)
}
