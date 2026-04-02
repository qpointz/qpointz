package io.qpointz.mill.autoconfigure.data.backend.flow;

import io.qpointz.mill.data.backend.flow.FlowDescriptorMetadataSource;
import io.qpointz.mill.data.backend.flow.SourceDefinitionRepository;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

import java.time.Duration;

import static io.qpointz.mill.autoconfigure.data.backend.BackendAutoConfiguration.MILL_DATA_BACKEND_CONFIG_KEY;

/**
 * Registers the flow YAML {@link io.qpointz.mill.metadata.source.MetadataSource} that contributes
 * {@code flow-*} inferred facets when the flow backend is active.
 */
@AutoConfiguration
@AutoConfigureAfter(FlowBackendAutoConfiguration.class)
@ConditionalOnClass(FlowDescriptorMetadataSource.class)
@ConditionalOnBean(SourceDefinitionRepository.class)
@ConditionalOnProperty(
        prefix = MILL_DATA_BACKEND_CONFIG_KEY + ".flow.metadata",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
public class FlowDescriptorMetadataSourceAutoConfiguration {

    /**
     * @param repository       flow descriptor repository (same bean as the Calcite schema factory)
     * @param flowBackendProperties cache toggles under {@code mill.data.backend.flow.cache.facets}
     * @return metadata source with origin id {@link io.qpointz.mill.metadata.source.MetadataOriginIds#FLOW}
     */
    @Bean
    @ConditionalOnMissingBean(FlowDescriptorMetadataSource.class)
    public FlowDescriptorMetadataSource flowDescriptorMetadataSource(
            SourceDefinitionRepository repository,
            FlowBackendProperties flowBackendProperties) {
        var facetsCache = flowBackendProperties.getCache().getFacets();
        Duration ttl = facetsCache.getTtl();
        return new FlowDescriptorMetadataSource(
                repository,
                facetsCache.isEnabled(),
                ttl);
    }
}
