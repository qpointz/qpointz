package io.qpointz.mill.services.configuration;

import io.qpointz.mill.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceHandlerConfig {

    @Bean
    public static ServiceHandler serviceHandler(@Autowired MetadataProvider metadataProvider,
                                                @Autowired ExecutionProvider executionProvider,
                                                @Autowired(required = false) SqlProvider sqlProvider,
                                                @Autowired(required = false) SecurityProvider securityProvider,
                                                @Autowired(required = false) PlanRewriteChain planRewriteChain)
    {
        return new ServiceHandler(metadataProvider, executionProvider, sqlProvider, securityProvider, planRewriteChain);
    }

}
