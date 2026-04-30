package io.qpointz.mill.ai.service

import org.springframework.boot.autoconfigure.SpringBootApplication

/**
 * Spring Boot anchor for mill-ai-v3-service integration tests.
 *
 * Repository beans come from [org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration],
 * which scans packages registered by [io.qpointz.mill.persistence.configuration.PersistenceAutoConfiguration]
 * (`io.qpointz.mill.persistence`, including `...ai.jpa.repositories`). Do not exclude that auto-configuration here.
 */
@SpringBootApplication
class AiChatServiceITApplication
