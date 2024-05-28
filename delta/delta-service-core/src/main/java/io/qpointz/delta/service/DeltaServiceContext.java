package io.qpointz.delta.service;

import io.grpc.Channel;
import io.grpc.Server;
import io.qpointz.delta.proto.DeltaServiceGrpc;
import lombok.*;

@Builder
@AllArgsConstructor
public class DeltaServiceContext implements AutoCloseable {

    @Getter
    private String name;

    @Getter
    private DeltaService service;

    @Getter
    private Server server;

    @Getter
    private Channel channel;

    public DeltaServiceGrpc.DeltaServiceBlockingStub blocking() {
        return DeltaServiceGrpc.newBlockingStub(this.channel);
    }

    public DeltaServiceGrpc.DeltaServiceStub async() {
        return DeltaServiceGrpc.newStub(this.channel);
    }

    @Override
    public void close() {
        server.shutdown();
    }
}
