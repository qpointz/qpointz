package io.qpointz.mill.ai.nlsql.configuration;

import io.qpointz.mill.service.annotations.ConditionalOnService;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Enables JPA repositories for the NL2SQL chat module.
 */
@Configuration
@EnableJpaRepositories(basePackages = "io.qpointz.mill.ai.nlsql")
@ConditionalOnService("ai-nl2data")
public class JPAConfiguration {
    /**
     * JPA repository configuration scoped to NL2SQL chat module.
     */
}
