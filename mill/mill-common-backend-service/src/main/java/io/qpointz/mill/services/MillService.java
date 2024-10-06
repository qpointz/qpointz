package io.qpointz.mill.services;

import io.grpc.Status;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import io.qpointz.mill.proto.*;
import io.qpointz.mill.services.configuration.MillServiceConfiguration;
import io.qpointz.mill.services.security.SecurityContextSecurityProvider;
import io.qpointz.mill.services.utils.SubstraitUtils;
import io.qpointz.mill.vectors.VectorBlockIterator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;
import java.util.function.Function;
import java.util.function.Supplier;

@Slf4j
@GrpcService
@SpringBootApplication
public class MillService extends MillServiceGrpc.MillServiceImplBase {

    @Bean
    public static SecurityProvider securityProvider() {
        return new SecurityContextSecurityProvider();
    }

    public MillService(@Autowired ServiceHandler serviceHandler) {
        this.serviceHandler = serviceHandler;
    }

    @Getter(AccessLevel.PROTECTED)
    private final ServiceHandler serviceHandler;

    protected <R,S> void replyOne(R request, StreamObserver<S> observer, Function<R, S> producer) {
        observer.onNext(producer.apply(request));
        observer.onCompleted();
    }

    private void traceRequest(String name, Supplier<String> toString) {
        if (log.isTraceEnabled()) {
            log.trace("{} request:{}", name, toString.get());
        }
    }

    @Override
    public void handshake(HandshakeRequest request, StreamObserver<HandshakeResponse> observer) {
        traceRequest("Handshake", request::toString);
        replyOne(request, observer, this.serviceHandler::handshake);
    }

    @Override
    public void listSchemas(ListSchemasRequest request, StreamObserver<ListSchemasResponse> responseObserver) {
        traceRequest("listSchemas", request::toString);
        replyOne(request, responseObserver, this.serviceHandler::listSchemas);
    }

    @Override
    public void getSchema(GetSchemaRequest request, StreamObserver<GetSchemaResponse> responseObserver) {
        traceRequest("getSchema", request::toString);
        replyOne(request, responseObserver, this.serviceHandler::getSchemaProto);
    }

    @Override
    public void parseSql(ParseSqlRequest request, StreamObserver<ParseSqlResponse> responseObserver) {
        traceRequest("parseSql", request::toString);
        replyOne(request, responseObserver, this.serviceHandler::parseSqlProto);
    }

    @Override
    public void execPlan(ExecPlanRequest request, StreamObserver<ExecQueryResponse> responseObserver) {
        traceRequest("execPlan", request::toString);
        val iterator = this.getServiceHandler().executeToIterator(request);
        streamResult(iterator, responseObserver);
    }


    @SneakyThrows
    protected io.substrait.plan.Plan convertProtoToPlan(io.substrait.proto.Plan plan) {
        return SubstraitUtils.protoToPlan(plan);
    }

    @SneakyThrows
    protected io.substrait.proto.Plan convertPlanToProto(io.substrait.plan.Plan plan) {
        return SubstraitUtils.planToProto(plan);
    }

    @Override
    public void execSql(ExecSqlRequest request, StreamObserver<ExecQueryResponse> responseObserver) {
        traceRequest("execSql", request::toString);
        val parseResult = parseSqlAndValidate(request.getStatement().getSql());
        execPlan(ExecPlanRequest.newBuilder()
                    .setPlan(convertPlanToProto(parseResult.getPlan()))
                    .setConfig(request.getConfig())
                    .build(), responseObserver);
    }

    private SqlProvider.ParseResult parseSqlAndValidate(String sql) {
        val parseResult = this.serviceHandler.parseSql(sql);
        if (parseResult.isSuccess())
            return parseResult;
        throw parseResult.getException();
    }

    protected static void streamResult(VectorBlockIterator iterator, StreamObserver<ExecQueryResponse> responseObserver) {
        var callObserver = (ServerCallStreamObserver<ExecQueryResponse>)responseObserver;
        while (iterator.hasNext()) {
            val vectorBlock = iterator.next();
            val resp = ExecQueryResponse.newBuilder()
                    .setVector(vectorBlock)
                    .build();
            callObserver.onNext(resp);
        }
        callObserver.onCompleted();
    }

    public static void run(MillServiceConfiguration configuration, String[] args) {
        val configs = new ArrayList<Class<?>>();
        configs.addAll(configuration.configs());
        val all = configs.toArray(new Class<?>[0]);
        SpringApplication.run(all, args);
    }

}