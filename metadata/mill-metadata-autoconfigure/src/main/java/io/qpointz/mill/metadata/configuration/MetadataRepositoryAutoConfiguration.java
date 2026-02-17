package io.qpointz.mill.metadata.configuration;

import io.qpointz.mill.metadata.repository.MetadataRepository;
import io.qpointz.mill.metadata.repository.file.FileMetadataRepository;
import io.qpointz.mill.metadata.repository.file.SpringResourceResolver;
import io.qpointz.mill.metadata.service.MetadataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ResourceLoader;

import java.util.Arrays;
import java.util.List;

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
        String pathConfig = properties.getFile().getPath();
        
        List<String> locations = Arrays.stream(pathConfig.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .collect(java.util.stream.Collectors.toList());
        
        log.info("Creating FileMetadataRepository with {} location(s): {}", locations.size(), locations);
        return new FileMetadataRepository(locations, new SpringResourceResolver(resourceLoader));
    }

    @Bean
    @ConditionalOnBean(MetadataRepository.class)
    public MetadataService metadataService(MetadataRepository repository) {
        return new MetadataService(repository);
    }
}

