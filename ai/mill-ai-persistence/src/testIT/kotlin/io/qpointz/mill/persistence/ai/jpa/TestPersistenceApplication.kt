package io.qpointz.mill.persistence.ai.jpa

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.persistence.autoconfigure.EntityScan

// JPA repositories: `mill-persistence-autoconfigure` registers `io.qpointz.mill.persistence` on
// AutoConfigurationPackages before DataJpaRepositoriesAutoConfiguration (Boot 4). Do not add
// @EnableJpaRepositories here — it would duplicate repository beans.
@SpringBootApplication
@EntityScan(basePackages = ["io.qpointz.mill.persistence"])
class TestPersistenceApplication
