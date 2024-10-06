package io.qpointz.mill.services;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.mockito.Mockito.mock;

@Configuration
public class ServiceBaseTestConfiguration {

    @Bean
    public SecurityProvider securityProvider() {
        return mock(SecurityProvider.class);
    }

    @Bean
    public SqlProvider sqlProvider() {
        return mock(SqlProvider.class);
    }

    @Bean
    public ExecutionProvider executionProvider() {
        return mock(ExecutionProvider.class);
    }

    @Bean
    public PlanRewriteChain rewriteChain() {
        return mock(PlanRewriteChain.class);
    }

    @Bean
    public MetadataProvider metadataProvider() {
        return mock(MetadataProvider.class);
    }

}
