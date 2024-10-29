package io.qpointz.mill.services.configuration;

import io.qpointz.mill.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DefaultServiceHandlerConfiguration {

    private final MetadataProvider metadataProvider;
    private final ExecutionProvider executionProvider;
    private final SqlProvider sqlProvider;
    private final SecurityProvider securityProvider;
    private final PlanRewriteChain planRewriteChain;

    public DefaultServiceHandlerConfiguration(
            @Autowired MetadataProvider metadataProvider,
            @Autowired ExecutionProvider executionProvider,
            @Autowired SqlProvider sqlProvider,
            @Autowired(required = false) SecurityProvider securityProvider,
            @Autowired(required = false) PlanRewriteChain planRewriteChain
    ) {
        this.metadataProvider = metadataProvider;
        this.executionProvider = executionProvider;
        this.sqlProvider = sqlProvider;
        this.securityProvider = securityProvider;
        this.planRewriteChain = planRewriteChain;
    }

    @Bean
    public ServiceHandler serviceHandler() {
        return new ServiceHandler(metadataProvider, executionProvider, sqlProvider, securityProvider, planRewriteChain);
    }

}
