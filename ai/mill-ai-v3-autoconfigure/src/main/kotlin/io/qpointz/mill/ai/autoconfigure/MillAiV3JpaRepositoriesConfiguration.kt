package io.qpointz.mill.ai.autoconfigure

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

/**
 * Enables Spring Data JPA repository interfaces for AI v3 persistence.
 *
 * Hosts that depend on [io.qpointz.mill.persistence.configuration.PersistenceAutoConfiguration]
 * (`mill-persistence-autoconfigure`) already declare
 * `@EnableJpaRepositories(basePackages = ["io.qpointz.mill.persistence"])`, which covers
 * `io.qpointz.mill.persistence.ai.jpa.repositories`. This configuration runs only when that
 * umbrella auto-configuration is absent (for example `apps/mill-service` with security and metadata
 * JPA only) so AI repositories are still discovered without duplicate bean definitions.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnMissingClass("io.qpointz.mill.persistence.configuration.PersistenceAutoConfiguration")
@EnableJpaRepositories(basePackages = ["io.qpointz.mill.persistence.ai.jpa.repositories"])
internal class MillAiV3JpaRepositoriesConfiguration
