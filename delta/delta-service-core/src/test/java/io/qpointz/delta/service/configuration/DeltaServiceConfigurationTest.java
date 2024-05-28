package io.qpointz.delta.service.configuration;

import io.qpointz.delta.service.ExecutionProvider;
import io.qpointz.delta.service.MetadataProvider;
import io.qpointz.delta.service.SqlProvider;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DeltaServiceConfigurationTest {

    public static class TestSqlConfig implements SqlProviderConfig {
        @Bean
        public SqlProvider sqlProvider() {
            return null;
        }
    }

    public static class TestMetadataConfig implements MetadataProviderConfig {

        @Bean
        public MetadataProvider metadataProvider() {
            return null;
        }
    }

    public static class TestExecutionConfig implements ExecutionProviderConfig {

        @Bean
        public ExecutionProvider executionProvider() {
            return null;
        }
    }

    public static class TestAllConfigs implements ProvidersConfig {

        @Bean
        public ExecutionProvider executionProvider() {
            return null;
        }

        @Bean
        public MetadataProvider metadataProvider() {
            return null;
        }

        @Bean
        public SqlProvider sqlProvider() {
            return null;
        }
    }

    @Configuration
    public static class TestConfig1 {}

    @Configuration
    public static class TestConfig2 {}


    @Test
    void individualProviders() {
        val config =  DeltaServiceConfiguration.newConfiguration()
                .withSqlProvider(TestSqlConfig.class)
                .withMetadataProvider(TestMetadataConfig.class)
                .withExecutionProvider(TestExecutionConfig.class);

        checkConfig(config,
                Set.of(TestExecutionConfig.class, TestSqlConfig.class, TestMetadataConfig.class),
                TestMetadataConfig.class,
                TestSqlConfig.class,
                TestExecutionConfig.class
        );
    }

    @Test
    void allProviders() {
        val config =  DeltaServiceConfiguration.newConfiguration()
                .withProviders(TestAllConfigs.class);

        checkConfig(config,
                Set.of(TestAllConfigs.class),
                TestAllConfigs.class,
                TestAllConfigs.class,
                TestAllConfigs.class
        );
    }


    @Test
    void missingProviderThrows() {
        Executable config =  () ->  DeltaServiceConfiguration.newConfiguration()
                .withSqlProvider(TestSqlConfig.class)
                .configs();
        assertThrows(IllegalArgumentException.class, config);
    }

    @Test
    void emptyConfigThrows() {
        Executable config =  () ->  DeltaServiceConfiguration.newConfiguration().configs();
        assertThrows(IllegalArgumentException.class, config);
    }

    @Test
    void settingProviderResetsAllProviders() {
        val config = DeltaServiceConfiguration.newConfiguration()
                .withProviders(TestAllConfigs.class);
        checkConfig(config,
                Set.of(TestAllConfigs.class),
                TestAllConfigs.class,
                TestAllConfigs.class,
                TestAllConfigs.class
        );
        config.withSqlProvider(TestSqlConfig.class);
        assertNull(config.getMetadataProviderConfiguration());
        assertNull(config.getExecutionProviderConfiguration());
        assertThrows(IllegalArgumentException.class, () -> config.configs());
    }

    @Test
    void additionalConfigsIncluded() {
        val config = DeltaServiceConfiguration.newConfiguration()
                .withProviders(TestAllConfigs.class)
                .withAdditionalConfig(List.of(TestConfig1.class, TestConfig2.class))
                .withAdditionalConfig(TestConfig1.class);


        checkConfig(config,
                Set.of(TestAllConfigs.class, TestConfig1.class, TestConfig2.class),
                TestAllConfigs.class,
                TestAllConfigs.class,
                TestAllConfigs.class
        );
    }

    private void checkConfig(DeltaServiceConfiguration config,
                             Set<Class<?>> expectedConfigs,
                             Class<?> metadataConfig,
                             Class<?> sqlConfigClass,
                             Class<?> executionConfigClass) {
        assertEquals(expectedConfigs, config.configs());
        assertEquals(metadataConfig, config.getMetadataProviderConfiguration());
        assertEquals(sqlConfigClass, config.getSqlProviderConfiguration());
        assertEquals(executionConfigClass, config.getExecutionProviderConfiguration());
    }


}