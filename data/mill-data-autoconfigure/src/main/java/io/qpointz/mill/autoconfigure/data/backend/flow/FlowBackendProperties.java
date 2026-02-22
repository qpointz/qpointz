package io.qpointz.mill.autoconfigure.data.backend.flow;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

import static io.qpointz.mill.autoconfigure.data.backend.BackendAutoConfiguration.MILL_DATA_BACKEND_CONFIG_KEY;

@Getter
@Setter
@ConfigurationProperties(prefix = MILL_DATA_BACKEND_CONFIG_KEY + ".flow")
public class FlowBackendProperties {

    /**
     * Paths to source descriptor YAML files.
     * Each descriptor becomes a Calcite schema whose name is the descriptor's {@code name} property.
     */
    private List<String> sources = new ArrayList<>();

}
