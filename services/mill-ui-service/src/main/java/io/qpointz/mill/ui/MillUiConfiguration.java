package io.qpointz.mill.ui;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Registers {@link MillUiProperties} for binding {@code mill.ui.*} settings.
 */
@Configuration
@EnableConfigurationProperties(MillUiProperties.class)
public class MillUiConfiguration {
}
