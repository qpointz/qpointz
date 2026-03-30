package io.qpointz.mill.metadata.configuration

import io.qpointz.mill.persistence.metadata.jpa.config.MetadataAuditBridgeRegistrar
import io.qpointz.mill.persistence.metadata.jpa.config.MetadataJpaBeansConfiguration
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Import
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

/**
 * Enables JPA persistence for greenfield metadata tables when `mill.metadata.repository.type=jpa`.
 *
 * Imports [MetadataJpaBeansConfiguration] (entity, facet, scope, facet-type, audit, seed adapters)
 * and [MetadataAuditBridgeRegistrar] so entity listeners can append to `metadata_audit`.
 */
@AutoConfiguration
@ConditionalOnClass(name = ["io.qpointz.mill.persistence.metadata.jpa.entities.MetadataEntityRecord"])
@ConditionalOnProperty(prefix = "mill.metadata.repository", name = ["type"], havingValue = "jpa")
@EntityScan(basePackages = ["io.qpointz.mill.persistence.metadata.jpa.entities"])
@EnableJpaRepositories(basePackages = ["io.qpointz.mill.persistence.metadata.jpa.repositories"])
@Import(
    MetadataJpaBeansConfiguration::class,
    MetadataAuditBridgeRegistrar::class
)
class MetadataJpaPersistenceAutoConfiguration
