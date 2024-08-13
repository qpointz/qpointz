package io.qpointz.mill.service.configuration;

import org.springframework.context.annotation.Configuration;

@Configuration
public interface ProvidersConfig extends SqlProviderConfig, MetadataProviderConfig, ExecutionProviderConfig {
}
