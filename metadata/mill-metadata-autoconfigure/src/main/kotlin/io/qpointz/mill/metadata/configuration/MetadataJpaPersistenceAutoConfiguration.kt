package io.qpointz.mill.metadata.configuration

import io.qpointz.mill.metadata.domain.MetadataChangeObserverDelegate
import io.qpointz.mill.metadata.repository.FacetTypeRepository
import io.qpointz.mill.metadata.repository.MetadataRepository
import io.qpointz.mill.persistence.metadata.jpa.adapters.JpaFacetTypeRepository
import io.qpointz.mill.persistence.metadata.jpa.adapters.JpaMetadataChangeObserver
import io.qpointz.mill.persistence.metadata.jpa.adapters.JpaMetadataRepository
import io.qpointz.mill.persistence.metadata.jpa.repositories.MetadataEntityJpaRepository
import io.qpointz.mill.persistence.metadata.jpa.repositories.MetadataFacetScopeJpaRepository
import io.qpointz.mill.persistence.metadata.jpa.repositories.MetadataFacetTypeJpaRepository
import io.qpointz.mill.persistence.metadata.jpa.repositories.MetadataOperationAuditJpaRepository
import io.qpointz.mill.persistence.metadata.jpa.repositories.MetadataScopeJpaRepository
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Bean
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

/**
 * Auto-configures the JPA-backed metadata persistence layer.
 *
 * Activated when:
 * - `io.qpointz.mill.persistence.metadata.jpa.entities.MetadataEntityRecord` is on the classpath
 * - `mill.metadata.storage.type=jpa` is set in application properties
 *
 * Registers three beans:
 * - [JpaMetadataRepository] as the [MetadataRepository] implementation
 * - [JpaFacetTypeRepository] as the [FacetTypeRepository] implementation
 * - [JpaMetadataChangeObserver] as a [MetadataChangeObserverDelegate] for audit persistence
 *
 * Entity scanning and JPA repository enablement are scoped to the
 * `io.qpointz.mill.persistence.metadata.jpa` sub-packages so they do not interfere with
 * other persistence contexts in the application.
 */
@AutoConfiguration
@ConditionalOnClass(name = ["io.qpointz.mill.persistence.metadata.jpa.entities.MetadataEntityRecord"])
@ConditionalOnProperty(prefix = "mill.metadata.storage", name = ["type"], havingValue = "jpa")
@EntityScan(basePackages = ["io.qpointz.mill.persistence.metadata.jpa.entities"])
@EnableJpaRepositories(basePackages = ["io.qpointz.mill.persistence.metadata.jpa.repositories"])
class MetadataJpaPersistenceAutoConfiguration {

    /**
     * Creates a [JpaMetadataRepository] when no other [MetadataRepository] bean is present.
     *
     * @param entityRepo     Spring Data repository for `metadata_entity`
     * @param facetScopeRepo Spring Data repository for `metadata_facet_scope`
     * @param scopeRepo      Spring Data repository for `metadata_scope`
     * @return the JPA-backed [MetadataRepository] implementation
     */
    @Bean
    @ConditionalOnMissingBean(MetadataRepository::class)
    fun jpaMetadataRepository(
        entityRepo: MetadataEntityJpaRepository,
        facetScopeRepo: MetadataFacetScopeJpaRepository,
        scopeRepo: MetadataScopeJpaRepository
    ): MetadataRepository = JpaMetadataRepository(entityRepo, facetScopeRepo, scopeRepo)

    /**
     * Creates a [JpaFacetTypeRepository] when no other [FacetTypeRepository] bean is present.
     *
     * @param jpaRepo Spring Data repository for `metadata_facet_type`
     * @return the JPA-backed [FacetTypeRepository] implementation
     */
    @Bean
    @ConditionalOnMissingBean(FacetTypeRepository::class)
    fun jpaFacetTypeRepository(
        jpaRepo: MetadataFacetTypeJpaRepository
    ): FacetTypeRepository = JpaFacetTypeRepository(jpaRepo)

    /**
     * Creates a [JpaMetadataChangeObserver] that persists audit entries to `metadata_operation_audit`.
     *
     * The bean implements [MetadataChangeObserverDelegate] (not [io.qpointz.mill.metadata.domain.MetadataChangeObserver])
     * so it is collected into the observer chain by [MetadataImportExportAutoConfiguration] without
     * creating a circular dependency.
     *
     * @param auditRepo Spring Data repository for `metadata_operation_audit`
     * @return the audit-persisting observer delegate
     */
    @Bean
    fun jpaMetadataChangeObserver(
        auditRepo: MetadataOperationAuditJpaRepository
    ): MetadataChangeObserverDelegate = JpaMetadataChangeObserver(auditRepo)
}
