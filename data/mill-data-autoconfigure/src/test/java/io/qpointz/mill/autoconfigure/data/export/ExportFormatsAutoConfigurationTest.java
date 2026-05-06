package io.qpointz.mill.autoconfigure.data.export;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Ensures bundled export format JARs are visible to {@link AggregateExportFormatRegistry}
 * via {@link java.util.ServiceLoader} when those JARs are on the application classpath.
 */
class ExportFormatsAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ExportFormatsAutoConfiguration.class));

    @Test
    void registryAggregatesBundledSpiProviders() {
        contextRunner.run(context -> {
            assertThat(context).hasNotFailed();
            AggregateExportFormatRegistry registry = context.getBean(AggregateExportFormatRegistry.class);
            assertThat(registry.allProviders())
                    .extracting(p -> p.metadata().getId().toLowerCase())
                    .contains("csv", "tsv", "avro", "json", "xlsx");
        });
    }
}
