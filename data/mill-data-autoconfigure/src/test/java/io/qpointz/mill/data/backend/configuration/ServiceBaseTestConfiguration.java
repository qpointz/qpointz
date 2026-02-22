package io.qpointz.mill.data.backend.configuration;

import io.qpointz.mill.data.backend.ExecutionProvider;
import io.qpointz.mill.data.backend.SchemaProvider;
import io.qpointz.mill.data.backend.SecurityProvider;
import io.qpointz.mill.data.backend.SqlProvider;
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
    public SchemaProvider metadataProvider() {
        return mock(SchemaProvider.class);
    }

}
