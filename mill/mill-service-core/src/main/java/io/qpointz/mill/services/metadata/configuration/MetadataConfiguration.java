package io.qpointz.mill.services.metadata.configuration;

import io.qpointz.mill.services.SchemaProvider;
import io.qpointz.mill.services.metadata.AnnotationsRepository;
import io.qpointz.mill.services.metadata.MetadataProvider;
import io.qpointz.mill.services.metadata.RelationsProvider;
import io.qpointz.mill.services.metadata.impl.NoneAnnotationsRepository;
import io.qpointz.mill.services.metadata.impl.NoneRelationsProvider;
import io.qpointz.mill.services.metadata.impl.file.FileAnnotationsRepository;
import io.qpointz.mill.services.metadata.impl.file.FileRelationsProvider;
import io.qpointz.mill.services.metadata.impl.MetadataProviderImpl;
import io.qpointz.mill.services.metadata.impl.file.FileRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;

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
    public FileRepository metadataFileRepository(@Value("${mill.metadata.file.repository.path}")Resource resource) throws IOException {
        return FileRepository.from(resource);
    }


}
