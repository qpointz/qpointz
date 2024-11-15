package io.qpointz.mill.services.configuration;

import io.qpointz.mill.services.*;
import io.qpointz.mill.services.dispatchers.*;
import io.substrait.extension.SimpleExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

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
                                                           MetadataProvider metadataProvider,
                                                           SecurityDispatcher securityDispatcher,
                                                           SubstraitDispatcher substraitDispatcher,
                                                           ResultAllocator resultAllocator) {
        return new DataOperationDispatcherImpl(metadataProvider, executionProvider, sqlProvider,
                securityDispatcher, planRewriteChain, substraitDispatcher, resultAllocator);
    }

    @Bean
    public PlanRewriteChain planRewriteChain(List<PlanRewriter> rewriters) {
        return new PlanRewriteChain(rewriters);
    }

    @Bean
    public PlanDispatcher planDispatcher(SimpleExtension.ExtensionCollection extensionCollection, MetadataProvider metadataProvider) {
        return new PlanDispatcherImpl(extensionCollection, metadataProvider);
    }


}
