package io.qpointz.mill.autoconfigure.data.backend;

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
 *   <li>{@code redact} — controls payload hygiene for inferred facets. Defaults to {@code BASIC}.</li>
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
     * Controls payload hygiene for inferred metadata facets.
     */
    private RedactMode redact = RedactMode.BASIC;

    /**
     * Redaction levels ordered by restrictiveness: {@code NONE} &lt; {@code BASIC} &lt; {@code SAFE}.
     */
    public enum RedactMode {
        /** Pass payloads through unchanged — may expose credentials. */
        NONE,
        /** Strip credential keys and sanitise URLs with embedded secrets. */
        BASIC,
        /** Emit only allow-listed structural keys ({@code type}, {@code bucket}, {@code region}, etc.). */
        SAFE
    }
}
