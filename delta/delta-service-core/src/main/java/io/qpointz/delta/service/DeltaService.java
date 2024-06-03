package io.qpointz.delta.service;

import io.grpc.Status;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import io.qpointz.delta.proto.*;
import io.qpointz.delta.service.configuration.DeltaServiceConfiguration;
import io.qpointz.delta.service.utils.SubstraitUtils;
import io.qpointz.delta.sql.VectorBlockIterator;
import io.substrait.proto.Plan;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.ArrayList;
import java.util.function.Function;
import java.util.function.Supplier;


@Slf4j
@GrpcService
@SpringBootApplication
public class DeltaService extends DeltaServiceGrpc.DeltaServiceImplBase {

    public DeltaService(@Autowired MetadataProvider metadataProvider,
                        @Autowired ExecutionProvider executionProvider,
                        @Autowired SqlProvider sqlProvider) {
        this.metadataProvider = metadataProvider;
        this.executionProvider = executionProvider;
        this.sqlProvider = sqlProvider;
        this.securityProvider = new SecurityProvider();
    }

    @Getter(AccessLevel.PROTECTED)
    private final MetadataProvider metadataProvider;

    @Getter(AccessLevel.PROTECTED)
    private final ExecutionProvider executionProvider;

    @Getter(AccessLevel.PROTECTED)
    private final SqlProvider sqlProvider;

    @Getter(AccessLevel.PROTECTED)
    private final SecurityProvider securityProvider;


    protected boolean supportsSql() {
        return this.sqlProvider != null;
    }

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
        replyOne(request, observer, r-> {
            val capabilities = HandshakeResponse.Capabilities.newBuilder()
                    .setSupportSql(this.supportsSql())
                    .build();

            val auth = HandshakeResponse.SecurityContext.newBuilder()
                    .setPrincipal(this.securityProvider.getPrincipalName())
                    .build();

            return HandshakeResponse.newBuilder()
                    .setVersion(ProtocolVersion.V1_0)
                    .setCapabilities(capabilities)
                    .setSecurity(auth)
                    .build();
        });
    }


    @Override
    public void listSchemas(ListSchemasRequest request, StreamObserver<ListSchemasResponse> responseObserver) {
        traceRequest("listSchemas", request::toString);
        replyOne(request, responseObserver, r-> ListSchemasResponse.newBuilder()
                .addAllSchemas(this.getMetadataProvider().getSchemaNames())
                .build());
    }

    @Override
    public void getSchema(GetSchemaRequest request, StreamObserver<GetSchemaResponse> responseObserver) {
        traceRequest("getSchema", request::toString);
        replyOne(request, responseObserver, r-> {
            val builder = GetSchemaResponse.newBuilder();
            val provider = this.getMetadataProvider();
            val requestedSchema = r.getSchemaName();
            val schema = this.getMetadataProvider().getSchema(requestedSchema);

            if (!provider.isSchemaExists(r.getSchemaName())) {
                val message = String.format("Schema '%s' not found",
                        requestedSchema == null ? "<root>" : requestedSchema);
                throw Status.NOT_FOUND
                        .augmentDescription(message)
                        .asRuntimeException();
            }
            return builder
                    .setSchema(schema)
                    .build();
        });
    }

    @Override
    public void parseSql(ParseSqlRequest request, StreamObserver<ParseSqlResponse> responseObserver) {
        traceRequest("parseSql", request::toString);
        replyOne(request, responseObserver, r -> {
            val parseResult = parseSql(request.getStatement().getSql());
            return ParseSqlResponse.newBuilder()
                    .setPlan(convertPlanToProto(parseResult.getPlan()))
                    .build();
        });
    }

    private SqlProvider.ParseResult parseSql(String sql) {
        if (!this.supportsSql()) {
            throw Status.UNIMPLEMENTED
                    .augmentDescription("SQL not supported")
                    .asRuntimeException();
        }

        val parseResult = this.sqlProvider.parseSql(sql);

        if (!parseResult.isSuccess()) {
            throw Status.INVALID_ARGUMENT
                    .augmentDescription(parseResult.getMessage())
                    .withCause(parseResult.getException())
                    .asRuntimeException();
        }

        return parseResult;
    }

    @Override
    public void execPlan(ExecPlanRequest request, StreamObserver<ExecQueryResponse> responseObserver) {
        traceRequest("execPlan", request::toString);
        val plan = convertProtoToPlan(request.getPlan());
        val config = request.getConfig();
        val iterator = this.executionProvider.execute(plan, config);
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
        val parseResult = parseSql(request.getStatement().getSql());
        execPlan(ExecPlanRequest.newBuilder()
                    .setPlan(convertPlanToProto(parseResult.getPlan()))
                    .setConfig(request.getConfig())
                    .build(), responseObserver);
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

    public static void run(DeltaServiceConfiguration configuration, String[] args) {
        val configs = new ArrayList<Class<?>>();
        configs.addAll(configuration.configs());
        val all = configs.toArray(new Class<?>[0]);
        SpringApplication.run(all, args);
    }

}