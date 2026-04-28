package io.qpointz.mill.ai.autoconfigure

import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

/**
 * Enables Spring Data JPA repository interfaces under `io.qpointz.mill.persistence.ai.jpa.repositories`.
 *
 * This is wired only via [AiV3JpaRepositoriesImportSelector]: when
 * `io.qpointz.mill.persistence.configuration.PersistenceAutoConfiguration` (`mill-persistence-autoconfigure`)
 * is on the classpath, it already declares `@EnableJpaRepositories` for `io.qpointz.mill.persistence`,
 * which includes these repositories — the selector skips this configuration to avoid duplicate repository
 * beans.
 */
@Configuration(proxyBeanMethods = false)
@EnableJpaRepositories(basePackages = ["io.qpointz.mill.persistence.ai.jpa.repositories"])
class AiV3JpaRepositoriesConfiguration
