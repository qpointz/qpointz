package io.qpointz.rapids.server.worker.config;

import java.util.Map;

public interface CalciteConfig {

    public enum CalciteMode {
        STANDARD("standard"),
        PARCELS("parcels");
        public final String label;
        private CalciteMode(String label) {
            this.label = label;
        }
    }

    interface StandardModeConfig {
        Map<String, String> properties();
    }

    CalciteMode mode();

    StandardModeConfig standard();

}
