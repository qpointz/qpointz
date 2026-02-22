package io.qpointz.mill.data.backend.metadata.configuration;

import io.qpointz.mill.data.backend.SchemaProvider;
import io.qpointz.mill.data.backend.metadata.AnnotationsRepository;
import io.qpointz.mill.data.backend.metadata.MetadataProvider;
import io.qpointz.mill.data.backend.metadata.RelationsProvider;
import io.qpointz.mill.data.backend.metadata.impl.NoneAnnotationsRepository;
import io.qpointz.mill.data.backend.metadata.impl.NoneRelationsProvider;
import io.qpointz.mill.data.backend.metadata.impl.file.FileAnnotationsRepository;
import io.qpointz.mill.data.backend.metadata.impl.file.FileRelationsProvider;
import io.qpointz.mill.data.backend.metadata.impl.MetadataProviderImpl;
import io.qpointz.mill.data.backend.metadata.impl.file.FileRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.Resource;

import java.io.IOException;

@Slf4j
@Configuration
public class MetadataConfiguration {

    @Bean
    @ConditionalOnMissingBean(MetadataProvider.class)
    @Lazy
    public MetadataProvider defaultMetadataProvider(SchemaProvider schemaProvider,
                                                    AnnotationsRepository annotationsRepository,
                                                    RelationsProvider relationsProvider) {
        return new MetadataProviderImpl(schemaProvider, annotationsRepository, relationsProvider);
    }

    @Bean
    @Lazy
    @ConditionalOnProperty(prefix = "mill.metadata.file.repository", name = "path")
    public FileRepository metadataFileRepository(@Value("${mill.metadata.file.repository.path}") Resource resource) throws IOException {
        log.info("Using file-based metadata repository at {}", resource.getURI());
        return FileRepository.from(resource.getInputStream());
    }

    @Bean
    @Lazy
    @ConditionalOnProperty(prefix = "mill.metadata", name = "annotations", havingValue = "file")
    public FileAnnotationsRepository fileAnnotationsRepository(FileRepository fileRepository) {
        return new FileAnnotationsRepository(fileRepository);
    }

    @Bean
    @Lazy
    @ConditionalOnProperty(prefix = "mill.metadata", name = "relations", havingValue = "file")
    public FileRelationsProvider fileRelationsProvider(FileRepository fileRepository) {
        return new FileRelationsProvider(fileRepository);
    }

    @Bean
    @Lazy
    @ConditionalOnProperty(prefix = "mill.metadata", name = "annotations", havingValue = "none")
    @ConditionalOnMissingBean(AnnotationsRepository.class)
    public NoneAnnotationsRepository noneAnnotationsRepository() {
        return new NoneAnnotationsRepository();
    }

    @Bean
    @Lazy
    @ConditionalOnProperty(prefix = "mill.metadata", name = "relations", havingValue = "none")
    @ConditionalOnMissingBean(RelationsProvider.class)
    public NoneRelationsProvider noneRelationsProvider() {
        return new NoneRelationsProvider();
    }

}
