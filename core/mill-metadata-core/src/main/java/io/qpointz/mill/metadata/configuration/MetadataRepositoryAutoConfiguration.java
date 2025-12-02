package io.qpointz.mill.metadata.configuration;

import io.qpointz.mill.metadata.repository.MetadataRepository;
import io.qpointz.mill.metadata.repository.file.FileMetadataRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;

/**
 * Auto-configuration for metadata repository.
 * Creates FileMetadataRepository by default if no other repository is configured.
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(MetadataProperties.class)
@ConditionalOnMissingBean(MetadataRepository.class)
@ConditionalOnProperty(
    prefix = "mill.metadata.v2.storage",
    name = "type",
    havingValue = "file",
    matchIfMissing = true  // Default to file if not specified
)
public class MetadataRepositoryAutoConfiguration {
    
    @Bean
    public MetadataRepository fileMetadataRepository(
        ResourceLoader resourceLoader,
        MetadataProperties properties
    ) {
        String location = properties.getFile().getPath();
        log.info("Creating FileMetadataRepository with location: {}", location);
        return new FileMetadataRepository(location, resourceLoader);
    }
}

