package io.qpointz.mill.persistence.analysis.jpa

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.persistence.autoconfigure.EntityScan

// @EnableJpaRepositories is intentionally absent — [AnalysisPersistenceAutoConfiguration] supplies
// repository scanning for production (mill-service); in this slice test, Boot also scans from the
// @SpringBootApplication package below.
@SpringBootApplication
@EntityScan(basePackages = ["io.qpointz.mill.persistence.analysis.jpa.entities"])
class TestAnalysisPersistenceApplication
