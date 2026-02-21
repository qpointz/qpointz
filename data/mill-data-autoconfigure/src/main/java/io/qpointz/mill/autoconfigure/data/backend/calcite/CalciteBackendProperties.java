package io.qpointz.mill.autoconfigure.data.backend.calcite;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import static io.qpointz.mill.autoconfigure.data.backend.BackendAutoConfiguration.MILL_DATA_BACKEND_CONFIG_KEY;

@Getter
@Setter
@ConfigurationProperties(prefix = MILL_DATA_BACKEND_CONFIG_KEY + ".calcite")
public class CalciteBackendProperties {

    /**
     * Path to Calcite model file for schema definition.
     */
    public String model;

}
