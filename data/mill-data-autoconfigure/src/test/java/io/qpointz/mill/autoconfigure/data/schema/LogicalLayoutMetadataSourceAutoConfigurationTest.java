package io.qpointz.mill.autoconfigure.data.schema;

import io.qpointz.mill.autoconfigure.data.SqlAutoConfiguration;
import io.qpointz.mill.autoconfigure.data.backend.BackendAutoConfiguration;
import io.qpointz.mill.autoconfigure.data.backend.flow.FlowBackendAutoConfiguration;
import io.qpointz.mill.autoconfigure.data.backend.flow.FlowDescriptorMetadataSourceAutoConfiguration;
import io.qpointz.mill.data.backend.flow.FlowDescriptorMetadataSource;
import io.qpointz.mill.data.backend.dispatchers.SubstraitDispatcher;
import io.qpointz.mill.data.metadata.source.LogicalLayoutMetadataSource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Validates that {@link LogicalLayoutMetadataSourceAutoConfiguration} and
 * {@link FlowDescriptorMetadataSourceAutoConfiguration} respect the global
 * {@code mill.data.backend.metadata.enabled} kill-switch.
 */
class LogicalLayoutMetadataSourceAutoConfigurationTest {

    private ApplicationContextRunner flowContextRunner() {
        return new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(
                        SqlAutoConfiguration.class,
                        BackendAutoConfiguration.class,
                        FlowBackendAutoConfiguration.class,
                        FlowDescriptorMetadataSourceAutoConfiguration.class,
                        LogicalLayoutMetadataSourceAutoConfiguration.class
                ))
                .withBean(SubstraitDispatcher.class, () -> mock(SubstraitDispatcher.class))
                .withPropertyValues(
                        "mill.data.backend.type:flow",
                        "mill.data.backend.flow.sources[0]:/tmp/nonexistent.yaml"
                );
    }

    // -- LogicalLayoutMetadataSource registration --

    @Test
    void shouldRegisterLogicalLayoutByDefault() {
        flowContextRunner()
                .run(context -> assertThat(context).hasSingleBean(LogicalLayoutMetadataSource.class));
    }

    @Test
    void shouldRegisterLogicalLayoutWhenGlobalMetadataExplicitlyEnabled() {
        flowContextRunner()
                .withPropertyValues("mill.data.backend.metadata.enabled:true")
                .run(context -> assertThat(context).hasSingleBean(LogicalLayoutMetadataSource.class));
    }

    @Test
    void shouldOmitLogicalLayoutWhenGlobalMetadataDisabled() {
        flowContextRunner()
                .withPropertyValues("mill.data.backend.metadata.enabled:false")
                .run(context -> assertThat(context).doesNotHaveBean(LogicalLayoutMetadataSource.class));
    }

    // -- FlowDescriptorMetadataSource registration --

    @Test
    void shouldRegisterFlowDescriptorMetadataSourceByDefault() {
        flowContextRunner()
                .run(context -> assertThat(context).hasSingleBean(FlowDescriptorMetadataSource.class));
    }

    @Test
    void shouldOmitFlowDescriptorMetadataSourceWhenGlobalMetadataDisabled() {
        flowContextRunner()
                .withPropertyValues("mill.data.backend.metadata.enabled:false")
                .run(context -> assertThat(context).doesNotHaveBean(FlowDescriptorMetadataSource.class));
    }

    // -- Combined behavior --

    @Test
    void shouldOmitBothSourcesWhenGlobalMetadataDisabled() {
        flowContextRunner()
                .withPropertyValues("mill.data.backend.metadata.enabled:false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(LogicalLayoutMetadataSource.class);
                    assertThat(context).doesNotHaveBean(FlowDescriptorMetadataSource.class);
                });
    }

    @Test
    void shouldRegisterBothSourcesByDefault() {
        flowContextRunner()
                .run(context -> {
                    assertThat(context).hasSingleBean(LogicalLayoutMetadataSource.class);
                    assertThat(context).hasSingleBean(FlowDescriptorMetadataSource.class);
                });
    }
}
