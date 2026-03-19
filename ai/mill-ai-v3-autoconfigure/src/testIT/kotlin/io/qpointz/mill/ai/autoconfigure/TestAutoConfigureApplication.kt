package io.qpointz.mill.ai.autoconfigure

import org.springframework.boot.autoconfigure.SpringBootApplication

// Entity and repository scanning is delegated to PersistenceAutoConfiguration
// (from mill-persistence-autoconfigure on the runtime classpath), which registers
// @EntityScan and @EnableJpaRepositories for io.qpointz.mill.persistence and all
// sub-packages — covering the ai/v3 JPA entities and repositories.
@SpringBootApplication
class TestAutoConfigureApplication
