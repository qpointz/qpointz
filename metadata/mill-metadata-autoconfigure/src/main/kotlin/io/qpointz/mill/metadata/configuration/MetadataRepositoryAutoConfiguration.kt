package io.qpointz.mill.metadata.configuration

import io.qpointz.mill.metadata.repository.MetadataRepository
import io.qpointz.mill.metadata.repository.MetadataOperationAuditRepository
import io.qpointz.mill.metadata.repository.MetadataScopeRepository
import io.qpointz.mill.metadata.repository.NoOpMetadataRepository
import io.qpointz.mill.metadata.repository.NoOpMetadataOperationAuditRepository
import io.qpointz.mill.metadata.repository.NoOpMetadataScopeRepository
import io.qpointz.mill.metadata.repository.file.FileMetadataRepository
import io.qpointz.mill.metadata.repository.file.SpringResourceResolver
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.core.io.ResourceLoader

/**
 * Auto-configures the [MetadataRepository] and [MetadataScopeRepository] fallbacks.
 *
 * - `mill.metadata.storage.type=file` → [FileMetadataRepository] backed by YAML files
 * - No storage type configured → [NoOpMetadataRepository] / [NoOpMetadataScopeRepository],
 *   which return empty results and accept writes as silent no-ops, so dependant services
 *   start cleanly without requiring explicit persistence setup.
 */
@AutoConfiguration
@EnableConfigurationProperties(MetadataProperties::class)
class MetadataRepositoryAutoConfiguration {

    /**
     * Creates a [FileMetadataRepository] when `mill.metadata.storage.type=file`.
     *
     * @param resourceLoader Spring resource loader for resolving classpath and file paths
     * @param properties     bound metadata properties containing the file path list
     * @return a [FileMetadataRepository] reading from the configured locations
     */
    @Bean
    @ConditionalOnMissingBean(MetadataRepository::class)
    @ConditionalOnProperty(prefix = "mill.metadata.storage", name = ["type"], havingValue = "file")
    fun fileMetadataRepository(
        resourceLoader: ResourceLoader,
        properties: MetadataProperties
    ): MetadataRepository {
        val configuredPath = properties.file.path?.trim().orEmpty()
        if (configuredPath.isEmpty()) {
            throw IllegalStateException(
                "mill.metadata.storage.type=file requires non-empty mill.metadata.file.path"
            )
        }
        val locations = configuredPath.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
        if (locations.isEmpty()) {
            throw IllegalStateException(
                "mill.metadata.file.path resolved to zero locations; provide at least one resource path"
            )
        }
        log.info("Creating FileMetadataRepository with {} location(s): {}", locations.size, locations)
        return FileMetadataRepository(locations, SpringResourceResolver(resourceLoader))
    }

    /**
     * Registers [NoOpMetadataRepository] as the fallback when no other [MetadataRepository]
     * is present (i.e. neither JPA nor file storage is configured).
     *
     * @return the singleton [NoOpMetadataRepository]
     */
    @Bean
    @ConditionalOnMissingBean(MetadataRepository::class)
    fun noOpMetadataRepository(): MetadataRepository {
        log.info("No metadata storage configured — using NoOpMetadataRepository (empty results)")
        return NoOpMetadataRepository
    }

    /**
     * Registers [NoOpMetadataScopeRepository] as the fallback when no other
     * [MetadataScopeRepository] is present.
     *
     * @return the singleton [NoOpMetadataScopeRepository]
     */
    @Bean
    @ConditionalOnMissingBean(MetadataScopeRepository::class)
    fun noOpMetadataScopeRepository(): MetadataScopeRepository {
        log.info("No scope storage configured — using NoOpMetadataScopeRepository (empty results)")
        return NoOpMetadataScopeRepository
    }

    @Bean
    @ConditionalOnMissingBean(MetadataOperationAuditRepository::class)
    fun noOpMetadataOperationAuditRepository(): MetadataOperationAuditRepository {
        log.info("No audit storage configured — using NoOpMetadataOperationAuditRepository (empty results)")
        return NoOpMetadataOperationAuditRepository
    }

    companion object {
        private val log = LoggerFactory.getLogger(MetadataRepositoryAutoConfiguration::class.java)
    }
}
