package io.qpointz.mill.autoconfigure.data.backend.flow;

import io.qpointz.mill.autoconfigure.data.BaseDataAutoconfigurationTest;
import io.qpointz.mill.autoconfigure.data.SqlAutoConfiguration;
import io.qpointz.mill.autoconfigure.data.backend.BackendAutoConfiguration;
import io.qpointz.mill.data.backend.ExecutionProvider;
import io.qpointz.mill.data.backend.SchemaProvider;
import io.qpointz.mill.data.backend.SqlProvider;
import io.qpointz.mill.data.backend.calcite.CalciteContextFactory;
import io.qpointz.mill.data.backend.calcite.CalciteSqlDialectConventions;
import io.qpointz.mill.data.backend.calcite.providers.PlanConverter;
import io.qpointz.mill.data.backend.dispatchers.SubstraitDispatcher;
import io.qpointz.mill.data.backend.flow.FlowContextFactory;
import io.qpointz.mill.data.backend.flow.SourceDefinitionRepository;
import io.substrait.extension.ExtensionCollector;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class FlowBackendAutoConfigurationTest extends BaseDataAutoconfigurationTest {

    @Override
    protected ApplicationContextRunner configureContext(ApplicationContextRunner contextRunner) {
        return contextRunner
                .withConfiguration(AutoConfigurations.of(
                        SqlAutoConfiguration.class,
                        BackendAutoConfiguration.class,
                        FlowBackendAutoConfiguration.class
                ))
                .withBean(SubstraitDispatcher.class, () -> mock(SubstraitDispatcher.class))
                .withPropertyValues(
                        "mill.data.backend.type:flow",
                        "mill.data.backend.flow.sources[0]:/tmp/nonexistent.yaml"
                );
    }

    // -- conditional activation --

    @Test
    void shouldActivateWhenBackendTypeIsFlow() {
        contextRunner()
                .run(context -> assertThat(context).hasSingleBean(FlowBackendAutoConfiguration.class));
    }

    @Test
    void shouldNotActivateWhenBackendTypeIsMissing() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(
                        SqlAutoConfiguration.class,
                        BackendAutoConfiguration.class,
                        FlowBackendAutoConfiguration.class
                ))
                .run(context -> assertThat(context).doesNotHaveBean(FlowBackendAutoConfiguration.class));
    }

    @Test
    void shouldNotActivateWhenBackendTypeIsCalcite() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(
                        SqlAutoConfiguration.class,
                        BackendAutoConfiguration.class,
                        FlowBackendAutoConfiguration.class
                ))
                .withPropertyValues("mill.data.backend.type:calcite")
                .run(context -> assertThat(context).doesNotHaveBean(FlowBackendAutoConfiguration.class));
    }

    @Test
    void shouldNotActivateWhenBackendTypeIsJdbc() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(
                        SqlAutoConfiguration.class,
                        BackendAutoConfiguration.class,
                        FlowBackendAutoConfiguration.class
                ))
                .withPropertyValues("mill.data.backend.type:jdbc")
                .run(context -> assertThat(context).doesNotHaveBean(FlowBackendAutoConfiguration.class));
    }

    // -- FlowBackendProperties --

    @Test
    void shouldBindFlowProperties() {
        contextRunner().run(context -> {
            assertThat(context).hasSingleBean(FlowBackendProperties.class);
            var props = context.getBean(FlowBackendProperties.class);
            assertThat(props.getSources()).containsExactly("/tmp/nonexistent.yaml");
        });
    }

    // -- CalciteSqlDialectConventions bean --

    @Test
    void shouldProvideSqlDialectConventions() {
        contextRunner().run(context ->
                assertThat(context).hasSingleBean(CalciteSqlDialectConventions.class));
    }

    // -- SourceDefinitionRepository bean --

    @Test
    void shouldProvideSourceDefinitionRepository() {
        contextRunner().run(context ->
                assertThat(context).hasSingleBean(SourceDefinitionRepository.class));
    }

    // -- CalciteContextFactory bean --

    @Test
    void shouldProvideCalciteContextFactory() {
        contextRunner().run(context -> {
            assertThat(context).hasSingleBean(CalciteContextFactory.class);
            assertThat(context.getBean(CalciteContextFactory.class))
                    .isInstanceOf(FlowContextFactory.class);
        });
    }

    // -- ExtensionCollector bean --

    @Test
    void shouldProvideExtensionCollector() {
        contextRunner().run(context ->
                assertThat(context).hasSingleBean(ExtensionCollector.class));
    }

    // -- PlanConverter bean --

    @Test
    void shouldProvidePlanConverter() {
        contextRunner().run(context ->
                assertThat(context).hasSingleBean(PlanConverter.class));
    }

    // -- Service provider beans --

    @Test
    void shouldProvideSchemaProvider() {
        contextRunner().run(context ->
                assertThat(context).hasSingleBean(SchemaProvider.class));
    }

    @Test
    void shouldProvideExecutionProvider() {
        contextRunner().run(context ->
                assertThat(context).hasSingleBean(ExecutionProvider.class));
    }

    @Test
    void shouldProvideSqlProvider() {
        contextRunner().run(context ->
                assertThat(context).hasSingleBean(SqlProvider.class));
    }

    // -- FlowContextFactory wiring --

    @Test
    void shouldWireRepositoryIntoContextFactory() {
        contextRunner().run(context -> {
            var ctxFactory = context.getBean(CalciteContextFactory.class);
            assertThat(ctxFactory).isInstanceOf(FlowContextFactory.class);
            var flowFactory = (FlowContextFactory) ctxFactory;
            assertThat(flowFactory.getRepository())
                    .isSameAs(context.getBean(SourceDefinitionRepository.class));
        });
    }
}
