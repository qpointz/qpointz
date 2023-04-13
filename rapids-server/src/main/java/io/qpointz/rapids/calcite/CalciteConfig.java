package io.qpointz.rapids.calcite;

import com.google.common.collect.Lists;
import io.qpointz.rapids.config.PropertiesEntry;
import io.smallrye.config.ConfigMapping;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

@ApplicationScoped
@ConfigMapping(prefix=CalciteConfig.configurationPrefix)
public interface CalciteConfig {

    static String configurationPrefix =  "qpointz.rapids.calcite";
    public enum CalciteMode {
        STANDARD("standard"),
        PARCELS("parcels");
        public final String label;
        private CalciteMode(String label) {
            this.label = label;
        }
    }

    interface StandardModeConfig {
        @ConfigProperty(name="properties")
        List<PropertiesEntry> properties();

        default Properties getProperties() {
            return PropertiesEntry.asProperties(this.properties());
        }
    }

    CalciteMode mode();

    StandardModeConfig standard();

}
