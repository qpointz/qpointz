package io.qpointz.mill.persistence.configuration

import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.AutoConfigurationPackages
import org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration
import org.springframework.boot.persistence.autoconfigure.EntityScan
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar
import org.springframework.core.type.AnnotationMetadata

/**
 * Persistence bootstrap: entity scan plus Spring Data JPA repository discovery for all
 * `io.qpointz.mill.persistence.*` types.
 *
 * Spring Boot 4 wires JPA repositories via [org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration],
 * which scans [AutoConfigurationPackages]. A separate `@EnableJpaRepositories` on this class would
 * register the same repository beans twice (BeanDefinitionOverrideException). Repository discovery is
 * therefore driven only by registering the Mill persistence base package here.
 */
@AutoConfiguration(
    before = [DataJpaRepositoriesAutoConfiguration::class],
    after = [
        DataSourceAutoConfiguration::class,
        HibernateJpaAutoConfiguration::class,
    ],
)
@Import(PersistenceAutoConfiguration.PersistencePackagesRegistrar::class)
@EnableConfigurationProperties(MillPersistenceProperties::class)
@EntityScan(basePackages = ["io.qpointz.mill.persistence"])
class PersistenceAutoConfiguration {

    internal class PersistencePackagesRegistrar : ImportBeanDefinitionRegistrar {
        override fun registerBeanDefinitions(metadata: AnnotationMetadata, registry: BeanDefinitionRegistry) {
            AutoConfigurationPackages.register(registry, "io.qpointz.mill.persistence")
        }
    }
}
