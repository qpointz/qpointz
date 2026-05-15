package io.qpointz.mill.autoconfigure.data.resource;

import io.qpointz.mill.autoconfigure.data.backend.BackendAutoConfiguration;
import io.qpointz.mill.data.backend.resource.BackendResourceLoader;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ProtocolResolver;
import org.springframework.core.io.ResourceLoader;

/**
 * Registers a default {@link BackendResourceLoader} backed by a {@link DefaultResourceLoader} that
 * registers every {@link ProtocolResolver} bean (so {@code s3://}, {@code gs://}, and
 * {@code azure-blob://} work in servlet web applications where the context {@link ResourceLoader}
 * would otherwise treat those locations as servlet-relative paths).
 */
@AutoConfiguration
@AutoConfigureAfter(BackendAutoConfiguration.class)
public class BackendResourceLoaderAutoConfiguration {

    /**
     * @param applicationContext the Spring application context (for class loader and protocol resolver beans)
     * @return adapter delegating to a protocol-aware {@link ResourceLoader}
     */
    @Bean
    @ConditionalOnMissingBean(BackendResourceLoader.class)
    public BackendResourceLoader backendResourceLoader(ApplicationContext applicationContext) {
        DefaultResourceLoader resourceLoader = new DefaultResourceLoader(applicationContext.getClassLoader());
        applicationContext.getBeansOfType(ProtocolResolver.class).values().forEach(resourceLoader::addProtocolResolver);
        return new SpringResourceLoaderBackendResourceLoader(resourceLoader);
    }
}
