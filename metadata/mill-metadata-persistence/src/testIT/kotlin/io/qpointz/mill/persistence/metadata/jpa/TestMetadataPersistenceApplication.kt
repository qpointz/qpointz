package io.qpointz.mill.persistence.metadata.jpa

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan

// @EnableJpaRepositories is intentionally absent — Spring Boot's JpaRepositoriesAutoConfiguration
// scans from the @SpringBootApplication package (io.qpointz.mill.persistence.metadata.jpa), which
// covers all repos in the .repositories sub-package. Adding it explicitly causes duplicate
// bean registration alongside the autoconfigured scan.
@SpringBootApplication
@EntityScan(basePackages = ["io.qpointz.mill.persistence.metadata.jpa.entities"])
class TestMetadataPersistenceApplication
