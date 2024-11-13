package io.qpointz.mill.services.configuration;

import io.qpointz.mill.services.*;
import io.qpointz.mill.services.dispatchers.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import static org.mockito.Mockito.mock;

@Configuration
@Profile("!test-cmart")
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
    public MetadataProvider metadataProvider() {
        return mock(MetadataProvider.class);
    }

}
