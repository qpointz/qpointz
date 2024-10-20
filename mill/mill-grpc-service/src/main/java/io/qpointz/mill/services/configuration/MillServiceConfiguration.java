package io.qpointz.mill.services.configuration;

import io.qpointz.mill.services.MillService;
import io.qpointz.mill.services.MillServiceExceptionAdvice;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.devh.boot.grpc.server.autoconfigure.GrpcAdviceAutoConfiguration;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class MillServiceConfiguration {

    private MillServiceConfiguration() {

    }

    protected static final Class<?>[] DEFAULTS = {
            MillService.class,
            GrpcAdviceAutoConfiguration.class,
            MillServiceExceptionAdvice.class
    };

    public static MillServiceConfiguration newConfiguration() {
        return new MillServiceConfiguration();
    }

    private Class<?> metadataProviderConfiguration;

    private Class<?> sqlProviderConfiguration;

    private Class<?> executionProviderConfiguration;

    private Class<?> providersConfiguration;

    private Set<Class<?>> additionalConfigs = new HashSet<>();

    @Getter
    private String serviceName = "in-process-service";

    public MillServiceConfiguration withDefaults() {
        this.additionalConfigs.addAll(Arrays.asList(DEFAULTS));
        return this;
    }

    public Class<?> getMetadataProviderConfiguration() {
        return metadataProviderConfiguration !=null ? metadataProviderConfiguration : providersConfiguration;
    }

    public <T extends MetadataProviderConfig> MillServiceConfiguration withMetadataProvider(Class<T> config) {
        this.providersConfiguration = null;
        this.metadataProviderConfiguration = config;
        return this;
    }

    public Class<?> getExecutionProviderConfiguration() {
        return executionProviderConfiguration !=null ? executionProviderConfiguration : providersConfiguration;
    }

    public <T extends ExecutionProviderConfig> MillServiceConfiguration withExecutionProvider(Class<T> config) {
        this.providersConfiguration = null;
        this.executionProviderConfiguration = config;
        return this;
    }

    public Class<?> getSqlProviderConfiguration() {
        return sqlProviderConfiguration !=null ? sqlProviderConfiguration : providersConfiguration;
    }

    public <T extends SqlProviderConfig> MillServiceConfiguration withSqlProvider(Class<T> config) {
        this.providersConfiguration = null;
        this.sqlProviderConfiguration = config;
        return this;
    }

    public <T extends ProvidersConfig> MillServiceConfiguration withProviders(Class<T> config) {
        this.metadataProviderConfiguration = null;
        this.sqlProviderConfiguration = null;
        this.executionProviderConfiguration = null;
        this.providersConfiguration = config;
        return this;
    }

    private String okOrMissingMessage(Object obj) {
        return obj !=null ? "OK" : "MISSING";
    }

    public Collection<Class<?>> configs() {
        if (this.providersConfiguration == null && (this.executionProviderConfiguration==null || this.metadataProviderConfiguration == null || this.sqlProviderConfiguration == null)) {
            throw new IllegalArgumentException(String.format("All provider configuration must be provided sql:%s, execution:%s, metadata:%s"
                    , okOrMissingMessage(this.sqlProviderConfiguration)
                    , okOrMissingMessage(this.executionProviderConfiguration)
                    , okOrMissingMessage(this.metadataProviderConfiguration)));
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
            configs.stream().forEach(k-> log.debug("Mill service config:{}", k.getCanonicalName()));
        }

        return configs;
    }

    public Class<?>[] configsToArray() {
        return configs().toArray(new Class<?>[0]);
    }

    public MillServiceConfiguration withAdditionalConfig(Class<?> config) {
        this.additionalConfigs.add(config);
        return this;
    }

    public MillServiceConfiguration withAdditionalConfig(List<Class<?>> classes) {
        classes.stream()
                .forEach(this::withAdditionalConfig);
        return this;
    }

    public <T extends ProvidersConfig> MillServiceConfiguration enableSecurity() {
        return this.withAdditionalConfig(MillStandartSecurityConfig.class);
    }
}
