package io.qpointz.mill.persistence.analysis.jpa

import io.qpointz.mill.analysis.queries.SavedQueryCatalog
import io.qpointz.mill.persistence.analysis.jpa.adapters.JpaSavedQueryCatalog
import io.qpointz.mill.persistence.analysis.jpa.repositories.SavedQueryJpaRepository
import io.qpointz.mill.utils.JsonUtils
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.persistence.autoconfigure.EntityScan
import org.springframework.context.annotation.Bean
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

/**
 * Registers saved-query JPA entities, repositories, and {@link SavedQueryCatalog} (WI-256).
 *
 * Mirrors [io.qpointz.mill.persistence.security.jpa.configuration.SecurityJpaConfiguration]:
 * {@code mill-service} does not depend on {@code mill-persistence-autoconfigure}, so repository
 * discovery must be declared here rather than relying on a global persistence package scan.
 */
@AutoConfiguration
@ConditionalOnClass(SavedQueryJpaRepository::class)
@EntityScan(basePackages = ["io.qpointz.mill.persistence.analysis.jpa.entities"])
@EnableJpaRepositories(basePackages = ["io.qpointz.mill.persistence.analysis.jpa.repositories"])
class AnalysisPersistenceAutoConfiguration {

    /**
     * @param repository Spring Data repository for saved queries
     * @return catalog port backed by JPA
     */
    @Bean
    @ConditionalOnMissingBean(SavedQueryCatalog::class)
    fun savedQueryCatalog(
        repository: SavedQueryJpaRepository,
    ): SavedQueryCatalog = JpaSavedQueryCatalog(repository, JsonUtils.defaultJsonMapper())
}
