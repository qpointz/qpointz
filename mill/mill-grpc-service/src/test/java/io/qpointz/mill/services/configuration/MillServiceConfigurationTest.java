package io.qpointz.mill.services.configuration;

import io.qpointz.mill.services.ExecutionProvider;
import io.qpointz.mill.services.MetadataProvider;
import io.qpointz.mill.services.SqlProvider;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class MillServiceConfigurationTest {

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
        val config =  MillServiceConfiguration.newConfiguration()
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
        val config =  MillServiceConfiguration.newConfiguration()
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
        Executable config =  () ->  MillServiceConfiguration.newConfiguration()
                .withSqlProvider(TestSqlConfig.class)
                .configs();
        assertThrows(IllegalArgumentException.class, config);
    }

    @Test
    void emptyConfigThrows() {
        Executable config =  () ->  MillServiceConfiguration.newConfiguration().configs();
        assertThrows(IllegalArgumentException.class, config);
    }

    @Test
    void settingProviderResetsAllProviders() {
        val config = MillServiceConfiguration.newConfiguration()
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
        val config = MillServiceConfiguration.newConfiguration()
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

    @Test
    void testWithDefaults() {
        val config = MillServiceConfiguration.newConfiguration()
                .withProviders(TestAllConfigs.class)
                .withDefaults();

        val expected = new HashSet<Class<?>>();
        expected.addAll(Arrays.stream(MillServiceConfiguration.DEFAULTS).toList());
        expected.add(TestAllConfigs.class);

        checkConfig(config, expected,
                TestAllConfigs.class, TestAllConfigs.class, TestAllConfigs.class);

    }

    private void checkConfig(MillServiceConfiguration config,
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