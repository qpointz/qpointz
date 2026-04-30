package io.qpointz.mill.ai.v3.jpa

import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

/**
 * Enables Spring Data JPA repository interfaces under `io.qpointz.mill.persistence.ai.jpa.repositories`.
 *
 * Lives outside `io.qpointz.mill.ai.autoconfigure` so it is not component-scanned by a typical
 * `@SpringBootApplication` in that package. Normal Mill stacks use
 * [io.qpointz.mill.persistence.configuration.PersistenceAutoConfiguration] (`mill-persistence-autoconfigure`),
 * which already declares `@EnableJpaRepositories` for `io.qpointz.mill.persistence`, covering these
 * repositories. `@Import` this class only in a minimal host that has JPA and `mill-ai-v3-persistence`
 * but does not use `mill-persistence-autoconfigure`.
 */
@Configuration(proxyBeanMethods = false)
@EnableJpaRepositories(basePackages = ["io.qpointz.mill.persistence.ai.jpa.repositories"])
class AiV3JpaRepositoriesConfiguration
