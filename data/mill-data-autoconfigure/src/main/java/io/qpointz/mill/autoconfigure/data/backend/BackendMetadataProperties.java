package io.qpointz.mill.autoconfigure.data.backend;

import io.qpointz.mill.source.descriptor.StorageFacetRedactMode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import static io.qpointz.mill.autoconfigure.data.backend.BackendAutoConfiguration.MILL_DATA_BACKEND_CONFIG_KEY;

/**
 * Global metadata controls for all backend-contributed {@link io.qpointz.mill.metadata.source.MetadataSource} beans.
 *
 * <p>Prefix: {@code mill.data.backend.metadata}.
 *
 * <ul>
 *   <li>{@code enabled} — when {@code false}, no backend metadata sources are registered
 *       (global kill-switch). Defaults to {@code true}.</li>
 *   <li>{@code redact} — controls payload hygiene for inferred facets (see {@link StorageFacetRedactMode}).
 *       Defaults to {@code BASIC}.</li>
 * </ul>
 */
@Getter
@Setter
@ConfigurationProperties(prefix = MILL_DATA_BACKEND_CONFIG_KEY + ".metadata")
public class BackendMetadataProperties {

    /**
     * Global kill-switch for all backend {@code MetadataSource} beans.
     * When {@code false}, neither {@code LogicalLayoutMetadataSource} nor
     * {@code FlowDescriptorMetadataSource} are registered.
     */
    private boolean enabled = true;

    /**
     * Controls payload hygiene for flow inferred metadata (for example {@code storage} in flow-schema facets).
     */
    private StorageFacetRedactMode redact = StorageFacetRedactMode.BASIC;
}
