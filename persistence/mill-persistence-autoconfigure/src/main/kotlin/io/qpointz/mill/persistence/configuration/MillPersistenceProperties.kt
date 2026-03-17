package io.qpointz.mill.persistence.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "mill.persistence")
data class MillPersistenceProperties(
    val datasource: DatasourceProperties = DatasourceProperties(),
) {
    data class DatasourceProperties(
        /** Target database mode. Determines Flyway and dialect hints. Default: h2-postgres */
        val mode: String = "h2-postgres",
    )
}
