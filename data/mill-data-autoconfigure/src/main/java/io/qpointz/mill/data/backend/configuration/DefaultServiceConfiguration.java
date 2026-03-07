package io.qpointz.mill.data.backend.configuration;

import io.qpointz.mill.data.backend.*;
import io.qpointz.mill.data.backend.dispatchers.*;
import io.qpointz.mill.sql.v2.dialect.DialectRegistry;
import io.qpointz.mill.security.SecurityProvider;
import io.substrait.extension.SimpleExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.List;
import java.util.Locale;

@Configuration
public class DefaultServiceConfiguration {

    @Bean
    public ServiceHandler serviceHandler(ResultAllocator resultAllocator,
                                         SubstraitDispatcher substraitDispatcher,
                                         SecurityDispatcher securityDispatcher,
                                         DataOperationDispatcher dataOperationDispatcher) {
        return new ServiceHandler(dataOperationDispatcher, securityDispatcher, resultAllocator, substraitDispatcher);
    }


    @Bean
    public ResultAllocator resultAllocator() {
        return new ResultAllocatorImpl();
    }

    @Bean
    public SecurityDispatcher securityDispatcher(@Autowired(required = false) SecurityProvider securityProvider) {
        return new SecurityDispatcherImpl(securityProvider);
    }

    @Bean
    public SubstraitDispatcher substraitDispatcher(SimpleExtension.ExtensionCollection extensionCollection) {
        return new SubstraitDispatcher(extensionCollection);
    }

    @Bean
    public DataOperationDispatcher dataOperationDispatcher(@Autowired(required = false) SqlProvider sqlProvider,
                                                           @Autowired ExecutionProvider executionProvider,
                                                           @Autowired(required = false) PlanRewriteChain planRewriteChain,
                                                           @Autowired Environment environment,
                                                           SchemaProvider schemaProvider,
                                                           SecurityDispatcher securityDispatcher,
                                                           SubstraitDispatcher substraitDispatcher,
                                                           ResultAllocator resultAllocator) {
        final String configuredDialect = configuredDefaultDialect(environment);
        return new DataOperationDispatcherImpl(schemaProvider, executionProvider, sqlProvider,
                securityDispatcher, planRewriteChain, substraitDispatcher, resultAllocator,
                DialectRegistry.fromClasspathDefaults(), configuredDialect);
    }

    @Bean
    public PlanRewriteChain planRewriteChain(List<PlanRewriter> rewriters) {
        return new PlanRewriteChain(rewriters);
    }

    @Bean
    public PlanDispatcher planDispatcher(SimpleExtension.ExtensionCollection extensionCollection, SchemaProvider schemaProvider) {
        return new PlanDispatcherImpl(extensionCollection, schemaProvider);
    }

    private static String configuredDefaultDialect(Environment environment) {
        if (environment.containsProperty("mill.data.sql.dialect")) {
            final String fromCurrentKey = environment.getProperty("mill.data.sql.dialect");
            if (fromCurrentKey != null && !fromCurrentKey.isBlank()) {
                return fromCurrentKey.trim().toUpperCase(Locale.ROOT);
            }
        }
        throw new IllegalStateException(
                "No default SQL dialect configured. Set 'mill.data.sql.dialect'.");
    }


}
