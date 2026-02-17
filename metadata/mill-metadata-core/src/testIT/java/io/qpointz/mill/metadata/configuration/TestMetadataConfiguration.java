package io.qpointz.mill.metadata.configuration;

import io.qpointz.mill.metadata.repository.MetadataRepository;
import io.qpointz.mill.metadata.service.MetadataService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Test configuration to ensure MetadataService is created when MetadataRepository is available.
 */
@Configuration
public class TestMetadataConfiguration {
    
    @Bean
    @ConditionalOnBean(MetadataRepository.class)
    public MetadataService metadataService(MetadataRepository repository) {
        return new MetadataService(repository);
    }
}

