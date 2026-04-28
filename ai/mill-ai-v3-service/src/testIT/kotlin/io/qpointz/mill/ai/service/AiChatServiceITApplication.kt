package io.qpointz.mill.ai.service

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration

/** Spring Boot anchor for mill-ai-v3-service integration tests. */
@SpringBootApplication(
    exclude = [JpaRepositoriesAutoConfiguration::class],
)
class AiChatServiceITApplication
