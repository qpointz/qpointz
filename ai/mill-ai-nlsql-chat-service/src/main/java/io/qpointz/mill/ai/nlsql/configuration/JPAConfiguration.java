package io.qpointz.mill.ai.nlsql.configuration;

import io.qpointz.mill.services.annotations.ConditionalOnService;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "io.qpointz.mill.ai.nlsql")
@ConditionalOnService("ai-nl2data")
public class JPAConfiguration {
}
