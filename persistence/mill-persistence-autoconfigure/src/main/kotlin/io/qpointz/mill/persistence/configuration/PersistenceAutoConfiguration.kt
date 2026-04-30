package io.qpointz.mill.persistence.configuration

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration
import org.springframework.boot.persistence.autoconfigure.EntityScan
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@AutoConfiguration(
    after = [
        DataSourceAutoConfiguration::class,
        HibernateJpaAutoConfiguration::class,
    ]
)
@EnableConfigurationProperties(MillPersistenceProperties::class)
@EnableJpaRepositories(basePackages = ["io.qpointz.mill.persistence"])
@EntityScan(basePackages = ["io.qpointz.mill.persistence"])
class PersistenceAutoConfiguration
