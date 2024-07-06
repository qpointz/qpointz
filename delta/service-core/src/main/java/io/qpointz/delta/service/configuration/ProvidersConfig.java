package io.qpointz.delta.service.configuration;

import org.springframework.context.annotation.Configuration;

@Configuration
public interface ProvidersConfig extends SqlProviderConfig, MetadataProviderConfig, ExecutionProviderConfig {
}
