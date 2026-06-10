package io.qpointz.mill.ai.autoconfigure.config;

import java.time.Duration;

/**
 * Startup and scheduled refresh settings for a {@code mill.ai.data.embedding} profile.
 */
public class RefreshSettings {

    private final OnStartup onStartup = new OnStartup();
    private final Schedule schedule = new Schedule();

    public OnStartup getOnStartup() {
        return onStartup;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    /**
     * {@code refresh.on-startup.*}.
     */
    public static class OnStartup {

        private boolean enabled = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    /**
     * {@code refresh.schedule.*}.
     */
    public static class Schedule {

        private boolean enabled = true;

        private Duration interval = Duration.ofMinutes(15);

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public Duration getInterval() {
            return interval;
        }

        public void setInterval(Duration interval) {
            this.interval = interval;
        }
    }
}
