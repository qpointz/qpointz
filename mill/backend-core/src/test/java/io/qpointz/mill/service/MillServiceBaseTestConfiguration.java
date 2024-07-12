package io.qpointz.mill.service;

import io.qpointz.mill.proto.MillServiceGrpc;
import net.devh.boot.grpc.client.autoconfigure.GrpcClientAutoConfiguration;
import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.server.autoconfigure.GrpcAdviceAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.mockito.Mockito.mock;

@Configuration
@ImportAutoConfiguration({
        GrpcAdviceAutoConfiguration.class,
        GrpcClientAutoConfiguration.class,
})
public class MillServiceBaseTestConfiguration {

    @GrpcClient("test")
    public MillServiceGrpc.MillServiceBlockingStub blockingStub;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public MillServiceGrpc.MillServiceBlockingStub blockingStub() {
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
