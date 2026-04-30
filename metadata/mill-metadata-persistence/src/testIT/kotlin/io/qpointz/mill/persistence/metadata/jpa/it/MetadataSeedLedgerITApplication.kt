package io.qpointz.mill.persistence.metadata.jpa.it

import io.qpointz.mill.metadata.configuration.MetadataCoreConfiguration
import io.qpointz.mill.metadata.configuration.MetadataEntityServiceAutoConfiguration
import io.qpointz.mill.metadata.configuration.MetadataImportExportAutoConfiguration
import io.qpointz.mill.metadata.configuration.MetadataSeedAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.persistence.autoconfigure.EntityScan
import org.springframework.context.annotation.Import

/**
 * JPA metadata persistence plus metadata autoconfigure (seed pipeline) for integration tests.
 *
 * <p>Lives outside {@code io.qpointz.mill.persistence.metadata.jpa} so it does not clash with
 * [io.qpointz.mill.persistence.metadata.jpa.TestMetadataPersistenceApplication] during default
 * configuration discovery.
 */
@SpringBootApplication(
    scanBasePackages = [
        "io.qpointz.mill.persistence.metadata.jpa.entities",
        "io.qpointz.mill.persistence.metadata.jpa.repositories",
        "io.qpointz.mill.persistence.metadata.jpa.adapters",
        "io.qpointz.mill.persistence.metadata.jpa.config"
    ],
    excludeName = ["io.qpointz.mill.autoconfigure.data.backend.calcite.CalciteBackendAutoConfiguration"]
)
@EntityScan(basePackages = ["io.qpointz.mill.persistence.metadata.jpa.entities"])
@Import(
    MetadataCoreConfiguration::class,
    MetadataImportExportAutoConfiguration::class,
    MetadataEntityServiceAutoConfiguration::class,
    MetadataSeedAutoConfiguration::class
)
class MetadataSeedLedgerITApplication
