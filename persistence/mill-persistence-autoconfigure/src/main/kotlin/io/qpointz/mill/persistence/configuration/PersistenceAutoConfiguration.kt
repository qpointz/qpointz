package io.qpointz.mill.persistence.configuration

import io.qpointz.mill.persistence.SchemaInfoRepository
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@AutoConfiguration(
    after = [
        DataSourceAutoConfiguration::class,
        HibernateJpaAutoConfiguration::class,
        FlywayAutoConfiguration::class,
    ]
)
@EnableConfigurationProperties(MillPersistenceProperties::class)
@EnableJpaRepositories(basePackageClasses = [SchemaInfoRepository::class])
@EntityScan(basePackages = ["io.qpointz.mill.persistence"])
class PersistenceAutoConfiguration
