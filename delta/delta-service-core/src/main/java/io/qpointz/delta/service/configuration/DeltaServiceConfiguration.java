package io.qpointz.delta.service.configuration;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class DeltaServiceConfiguration {

    private DeltaServiceConfiguration() {

    }

    public static DeltaServiceConfiguration newConfiguration() {
        return new DeltaServiceConfiguration();
    }

    private Class<?> metadataProviderConfiguration;

    private Class<?> sqlProviderConfiguration;

    private Class<?> executionProviderConfiguration;

    private Class<?> providersConfiguration;

    private Set<Class<?>> additionalConfigs = new HashSet<>();

    @Getter
    private String serviceName = "in-process-service";

    public DeltaServiceConfiguration withServiceName(String name) {
        this.serviceName = name;
        return this;
    }

    public Class<?> getMetadataProviderConfiguration() {
        return metadataProviderConfiguration !=null ? metadataProviderConfiguration : providersConfiguration;
    }

    public <T extends MetadataProviderConfig> DeltaServiceConfiguration withMetadataProvider(Class<T> config) {
        this.providersConfiguration = null;
        this.metadataProviderConfiguration = config;
        return this;
    }

    public Class<?> getExecutionProviderConfiguration() {
        return executionProviderConfiguration !=null ? executionProviderConfiguration : providersConfiguration;
    }

    public <T extends ExecutionProviderConfig> DeltaServiceConfiguration withExecutionProvider(Class<T> config) {
        this.providersConfiguration = null;
        this.executionProviderConfiguration = config;
        return this;
    }

    public Class<?> getSqlProviderConfiguration() {
        return sqlProviderConfiguration !=null ? sqlProviderConfiguration : providersConfiguration;
    }

    public <T extends SqlProviderConfig> DeltaServiceConfiguration withSqlProvider(Class<T> config) {
        this.providersConfiguration = null;
        this.sqlProviderConfiguration = config;
        return this;
    }

    public <T extends ProvidersConfig> DeltaServiceConfiguration withProviders(Class<T> config) {
        this.metadataProviderConfiguration = null;
        this.sqlProviderConfiguration = null;
        this.executionProviderConfiguration = null;
        this.providersConfiguration = config;
        return this;
    }

    public Collection<Class<?>> configs() {
        if (this.providersConfiguration == null && (this.executionProviderConfiguration==null || this.metadataProviderConfiguration == null || this.sqlProviderConfiguration == null)) {
            throw new IllegalArgumentException(String.format("All provider configuration must be provided sql:%s, execution:%s, metadata:%s"
                    , this.providersConfiguration!=null || this.sqlProviderConfiguration!=null ? "OK" : "MISSING"
                    , this.providersConfiguration!=null || this.executionProviderConfiguration!=null ? "OK" : "MISSING"
                    , this.providersConfiguration!=null || this.metadataProviderConfiguration!=null ? "OK" : "MISSING" ));
        }

        val allConfigs = new ArrayList<Class<?>>();
        allConfigs.add(this.providersConfiguration);
        allConfigs.add(this.sqlProviderConfiguration);
        allConfigs.add(this.executionProviderConfiguration);
        allConfigs.add(this.metadataProviderConfiguration);
        allConfigs.addAll(this.additionalConfigs);

        val configs = allConfigs.stream()
                .filter(k-> k!=null)
                .collect(Collectors.toSet());

        if (log.isDebugEnabled()) {
            configs.stream().forEach(k-> log.debug("Delta service config:{}", k.getCanonicalName()));
        }

        return configs;
    }

    public DeltaServiceConfiguration withAdditionalConfig(Class<?> config) {
        this.additionalConfigs.add(config);
        return this;
    }

    public DeltaServiceConfiguration withAdditionalConfig(List<Class<?>> classes) {
        classes.stream()
                .forEach(this::withAdditionalConfig);
        return this;
    }
}
