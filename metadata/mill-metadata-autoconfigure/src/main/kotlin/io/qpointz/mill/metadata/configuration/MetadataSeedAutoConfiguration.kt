package io.qpointz.mill.metadata.configuration

import io.qpointz.mill.metadata.repository.MetadataSeedLedgerRepository
import io.qpointz.mill.metadata.service.MetadataImportService
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.core.Ordered
import org.springframework.core.io.DefaultResourceLoader
import org.springframework.core.io.ProtocolResolver
import org.springframework.core.annotation.Order

/**
 * Ordered startup seeds: `mill.metadata.seed.resources` applied via [MetadataImportService] with ledger skips (SPEC §14.1).
 */
@AutoConfiguration
@AutoConfigureAfter(
    MetadataImportExportAutoConfiguration::class,
    MetadataEntityServiceAutoConfiguration::class
)
@EnableConfigurationProperties(MetadataSeedProperties::class)
class MetadataSeedAutoConfiguration {

    /**
     * Builds [MetadataSeedStartup] with a [DefaultResourceLoader] that aggregates every
     * [ProtocolResolver] bean so cloud schemes work in servlet web applications.
     *
     * @param seedProperties ordered seed locations and failure policy
     * @param importService canonical YAML importer
     * @param ledger optional completion ledger
     * @param applicationContext context used to discover [ProtocolResolver] beans and the class loader
     * @return configured startup runner
     */
    @Bean
    fun metadataSeedStartup(
        seedProperties: MetadataSeedProperties,
        importService: MetadataImportService,
        ledger: MetadataSeedLedgerRepository,
        applicationContext: ApplicationContext,
    ): MetadataSeedStartup {
        val rl = DefaultResourceLoader((applicationContext as org.springframework.core.io.ResourceLoader).classLoader)
        applicationContext.getBeansOfType(ProtocolResolver::class.java).values.forEach { rl.addProtocolResolver(it) }
        return MetadataSeedStartup(seedProperties, importService, ledger, rl)
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE + 40)
    fun metadataSeedApplicationRunner(startup: MetadataSeedStartup): ApplicationRunner =
        ApplicationRunner { startup.run() }

}
