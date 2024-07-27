package io.qpointz.mill.service.jdbc.configuration;

import io.qpointz.mill.service.ExecutionProvider;
import io.qpointz.mill.service.MetadataProvider;
import io.qpointz.mill.service.SqlProvider;
import io.qpointz.mill.service.configuration.ProvidersConfig;
import io.qpointz.mill.service.jdbc.providers.JdbcExecutionProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JdbcServiceProvidersConfiguration implements ProvidersConfig {

    @Bean
    public ExecutionProvider jdbcExecutionProvider() {
        return new JdbcExecutionProvider();
    }

    @Bean
    public MetadataProvider jdbcMetadataProvider() {
        return null;
    }

    @Bean
    public SqlProvider jdbcSqlProvider() {
        return null;
    }

}
