package io.qpointz.mill.service;

import io.qpointz.mill.proto.DeltaServiceGrpc;
import net.devh.boot.grpc.client.autoconfigure.GrpcClientAutoConfiguration;
import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.server.autoconfigure.GrpcAdviceAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.mockito.Mockito.mock;

@Configuration
@ImportAutoConfiguration({
        GrpcAdviceAutoConfiguration.class,
        GrpcClientAutoConfiguration.class,
})
public class DeltaServiceBaseTestConfiguration {

    @GrpcClient("test")
    public DeltaServiceGrpc.DeltaServiceBlockingStub blockingStub;

    @Bean
    public DeltaServiceGrpc.DeltaServiceBlockingStub blockingStub() {
        return blockingStub;
    }

    @Bean
    public SqlProvider sqlProvider() {
        return mock(SqlProvider.class);
    }

    @Bean
    public ExecutionProvider executionProvider() {
        return mock(ExecutionProvider.class);
    }

    @Bean
    public MetadataProvider metadataProvider() {
        return mock(MetadataProvider.class);
    }

}
