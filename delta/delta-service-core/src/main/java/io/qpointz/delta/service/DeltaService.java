package io.qpointz.delta.service;

import io.grpc.Server;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import io.qpointz.delta.proto.*;
import io.qpointz.delta.service.configuration.DeltaServiceConfiguration;
import io.qpointz.delta.sql.VectorBlockIterator;
import io.substrait.plan.Plan;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.security.core.context.SecurityContextHolder;
import java.io.IOException;
import java.util.ArrayList;
import java.util.function.Function;


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

    }

    @Getter(AccessLevel.PROTECTED)
    private final MetadataProvider metadataProvider;

    @Getter(AccessLevel.PROTECTED)
    private final ExecutionProvider executionProvider;

    @Getter
    private final SqlProvider sqlProvider;


    private boolean supportsSql() {
        return this.sqlProvider != null;
    }

    @Override
    public void listSchemas(ListSchemasRequest request, StreamObserver<ListSchemasResponse> responseObserver) {
        log.trace("Listing schemas");
        process(request, responseObserver, this::onListSchemas);
    }

    protected ListSchemasResponse onListSchemas(ListSchemasRequest listSchemasRequest) {
        return ListSchemasResponse.newBuilder()
                .setStatus(ResponseStatuses.ok())
                .addAllSchemas(this.getMetadataProvider().getSchemaNames())
                .build();
    }

    @Override
    public void handshake(HandshakeRequest request, StreamObserver<HandshakeResponse> responseObserver) {
        log.trace("Handshake request {}", request.toString());
        process(request, responseObserver, this::onHandshake);
    }

    protected HandshakeResponse onHandshake(HandshakeRequest request) {
        val capabilities = HandshakeResponse.Capabilities.newBuilder()
                .setSupportSql(this.supportsSql())
                .build();

        val authCtx = SecurityContextHolder.getContext();
        var principal = "ANONYMOUS";
        if (authCtx!=null && authCtx.getAuthentication()!=null) {
            principal = authCtx.getAuthentication().getPrincipal().toString();
        }

        val auth = HandshakeResponse.SecurityContext.newBuilder()
                .setPrincipal(principal)
                .build();

        return HandshakeResponse.newBuilder()
                .setVersion(ProtocolVersion.V1_0)
                .setStatus(ResponseStatuses.ok())
                .setCapabilities(capabilities)
                .setSecurity(auth)
                .build();
    }


    @Override
    public void getSchema(GetSchemaRequest request, StreamObserver<GetSchemaResponse> responseObserver) {
        process(request, responseObserver, this::onGetSchema);
    }

    protected GetSchemaResponse onGetSchema(GetSchemaRequest request) {
        val builder = GetSchemaResponse.newBuilder();
        val schema = this.getMetadataProvider().getSchema(request.getSchemaName());

        if (schema.isEmpty()) {
            val message = String.format("Schema '%s' not found", request.getSchemaName());
            return builder
                    .setStatus(ResponseStatuses.invalidRequest(message))
                    .build();
        } else {
            return builder
                    .setStatus(ResponseStatuses.ok())
                    .setSchema(schema.get())
                    .build();
        }
    }

    @Override
    public void parseSql(ParseSqlRequest request, StreamObserver<ParseSqlResponse> responseObserver) {
        process(request, responseObserver, this::onParseSql);
    }

    private ParseSqlResponse onParseSql(ParseSqlRequest parseSqlRequest) {
        if (!this.supportsSql()) {
            return ParseSqlResponse.newBuilder()
                    .setStatus(ResponseStatuses.notSupported("SQL not supported"))
                    .build();
        }

        val parseResult = this.sqlProvider.parseSql(parseSqlRequest.getStatement().getSql());
        val builder= ParseSqlResponse.newBuilder()
                .setStatus(ResponseStatuses.ok());

        if (parseResult.isSuccess() && parseResult.getException()==null ) {
            builder
                    .setPlan(parseResult.getPlan())
                    .setSuccess(true);
        } else {
            builder.setErrorMessage(parseResult.getMessage())
                    .setSuccess(false);
        }
        return builder.build();
    }

    @Override
    public void execPlan(ExecPlanRequest request, StreamObserver<ExecQueryResponse> responseObserver) {
        onExecPlan(request, responseObserver);
    }

    private void onExecPlan(ExecPlanRequest request, StreamObserver<ExecQueryResponse> responseObserver) {
        Plan plan = null;
        try {
            plan = this.executionProvider.protoToPlan(request.getPlan());
        } catch (IOException e) {
            process(responseObserver, ExecQueryResponse.newBuilder()
                    .setStatus(ResponseStatuses.serverError(e))
                    .build()
            );
            return;
        }

        val config = request.getConfig();
        val iterator = this.executionProvider.execute(plan, config);
        streamResult(iterator, responseObserver);
    }

    @Override
    public void execSql(ExecSqlRequest request, StreamObserver<ExecQueryResponse> responseObserver) {
        onExecSql(request, responseObserver);
    }

    protected void onExecSql(ExecSqlRequest execSqlRequest, StreamObserver<ExecQueryResponse> responseObserver) {
        val parseRequest = ParseSqlRequest.newBuilder()
                .setStatement(execSqlRequest.getStatement())
                .build();

        val parseResponse = onParseSql(parseRequest);

        if (parseResponse.getStatus().getCode() != ResponseCode.OK) {
            process(responseObserver, ExecQueryResponse.newBuilder()
                    .setStatus(parseResponse.getStatus())
                    .build());
            return ;
        }

        if (!parseResponse.getSuccess()) {
            val status = ResponseStatuses
                    .executionFailed(parseResponse.getErrorMessage());
            process(responseObserver, ExecQueryResponse.newBuilder()
                    .setStatus(status)
                    .build());
            return;
        }

        onExecPlan(ExecPlanRequest.newBuilder()
                .setPlan(parseResponse.getPlan())
                .setConfig(execSqlRequest.getConfig())
                .build(), responseObserver);
    }

    protected static void streamResult(VectorBlockIterator iterator, StreamObserver<ExecQueryResponse> responseObserver) {
        var callObserver = (ServerCallStreamObserver<ExecQueryResponse>)responseObserver;

        while (iterator.hasNext()) {
            val vectorBlock = iterator.next();
            val resp = ExecQueryResponse.newBuilder()
                    .setStatus(ResponseStatuses.ok())
                    .setVector(vectorBlock)
                    .build();
            callObserver.onNext(resp);
        }
        callObserver.onCompleted();
    }

    protected static <TResp> void process(StreamObserver<TResp> observer, TResp reply) {
        if (reply!=null) {
            observer.onNext(reply);
            observer.onCompleted();
        } else {
            observer.onError(new NullPointerException("Null response"));
        }
    }

    protected static <TReq, TResp> void process(TReq request, StreamObserver<TResp> observer, Function<TReq, TResp> onHandle) {
        try {
            final var reply = onHandle.apply(request);
            process(observer, reply);
        } catch (Exception ex) {
            log.error("Server error", ex);
            observer.onError(ex);
        }
    }


    public static void run(DeltaServiceConfiguration configuration, String[] args) {
        val configs = new ArrayList<Class<?>>();
        configs.addAll(configuration.configs());
        configs.add(DeltaService.class);
        val all = configs.toArray(new Class<?>[0]);
        SpringApplication.run(all, args);
    }

    public static DeltaServiceContext inProcess(DeltaServiceConfiguration configuration) {
        val appCtx = new AnnotationConfigApplicationContext();
        appCtx.register(configuration.configs().toArray(new Class<?>[0]));
        appCtx.register(DeltaService.class);
        val svc = appCtx.getBean(DeltaService.class);
        return inProcess(svc, configuration.getServiceName());
    }

    public static <T extends DeltaService> DeltaServiceContext inProcess(T deltaService) {
        return inProcess(deltaService, "delta-service");
    }

    public static <T extends DeltaService> DeltaServiceContext inProcess(T deltaService, String serviceName) {
        Server server = null;
        try {
            server = InProcessServerBuilder.forName(serviceName)
                    .directExecutor()
                    .addService(deltaService)
                    .build()
                    .start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        val channel = InProcessChannelBuilder
                .forName(serviceName)
                .directExecutor()
                .usePlaintext()
                .build();

        val ctx = DeltaServiceContext.builder()
                .service(deltaService)
                .server(server)
                .channel(channel)
                .build();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (ctx!=null && ctx.getServer()!= null) {
                ctx.getServer().shutdown();
            }
        }));

        return ctx;
    }

}


//    @AllArgsConstructor
//    @Builder
//    public static class PrepareResult {
//
//        @Getter
//        private Collection<String> errors;
//
//        @Getter
//        private PreparedStatement statement;
//
//        public boolean isSuccess() {
//            return errors==null || errors.size()==0;
//        }
//
//        public static PrepareResult of(Collection<String> errors) {
//            return PrepareResult.builder()
//                    .errors(errors)
//                    .build();
//        }
//
//        public static PrepareResult of(String error) {
//            return PrepareResult.of(List.of(error));
//        }
//
//        public static PrepareResult ok(PreparedStatement preparedStatement) {
//            return PrepareResult.builder()
//                    .errors(List.of())
//                    .statement(preparedStatement)
//                    .build();
//        }
//    }

//    protected PrepareStatementResponse onPrepareStatement(PrepareStatementRequest request) {
//        val statement = request.getStatement();
//
//        val builder = PrepareStatementResponse.newBuilder()
//                .setOriginalStatement(statement);
//
//        PrepareResult preparedResult = prepareStatement(statement);
//
//        if (preparedResult.isSuccess()) {
//            return builder
//                    .setStatus(ResponseStatuses.ok())
//                    .setStatement(preparedResult.getStatement())
//                    .build();
//        } else {
//            return builder
//                    .setStatus(ResponseStatuses.error("Validation error", preparedResult.getErrors()))
//                    .build();
//        }
//
//    }

//    private PrepareResult prepareStatement(Statement statement) {
//        if (statement.hasSql()) {
//            return prepareAndValidateSql(statement.getSql());
//        }
//
//        if (statement.hasText()) {
//            return prepareAndValidateText(statement.getText());
//        }
//
//        if (statement.hasPlan()) {
//            return prepareAndValidatePlan(statement.getPlan());
//        }
//
//        return PrepareResult.of("Statement expected to provide Sql or Substrait or Text plan.");
//    }
//
//    private PrepareResult prepareAndValidatePlan(PlanStatement statement) {
//        if (!this.supportsSubstraitPlan()) {
//            return PrepareResult.of("Substrait plans not supported");
//        }
//
//        val preparedStatement = PreparedStatement.newBuilder()
//                .setPlan(statement)
//                .build();
//
//        return PrepareResult.ok(preparedStatement);
//    }

//    private PrepareResult prepareAndValidateText(TextPlanStatement statement) {
//        return PrepareResult.of("JSON plans not supported");
//    }
//
//    private PrepareResult prepareAndValidateSql(SQLStatement statement) {
//        if (!this.supportsSql()) {
//            return PrepareResult.of("SQL not supported");
//        }
//
//        val parseResult = this.getSqlParserProvider()
//                .parse(statement.getStatement());
//
//        val planStatement = PlanStatement.newBuilder()
//                .setPlan(parseResult.getPlan())
//                .build();
//
//        val preparedStatement = PreparedStatement.newBuilder()
//                .setPlan(planStatement)
//                .build();
//
//        if (!parseResult.isSuccess()) {
//            return PrepareResult.of(parseResult.getMessage());
//        } else {
//            return PrepareResult.ok(preparedStatement);
//        }
//    }

//    @Override
//    public void executeQueryStream(ExecQueryStreamRequest request, StreamObserver<ExecQueryResponse> respObserver) {
//        var observer = (ServerCallStreamObserver<ExecQueryResponse>)respObserver;
//        try {
//            val prepared = request.getStatement();
//            if (prepared.hasPlan()) {
//                if (!this.executionProvider.canExecuteSubstraitPlan()) {
//                    val resp = ExecQueryResponse.newBuilder()
//                            .setStatus(ResponseStatuses.error("Plan execution not suported"))
//                            .build();
//                    observer.onNext(resp);
//                    observer.onCompleted();
//                }
//
//                val executor = this.executionProvider
//                        .getSubstraitExecutionProvider();
//                val iterator = executor.execute(prepared.getPlan(), request.getBatchSize());
//
//                streamResult(iterator, observer);
//                return;
//            }
//
//            if (prepared.hasSql()) {
//                if (!this.executionProvider.canExecuteSql()) {
//                    val resp = ExecQueryResponse.newBuilder()
//                            .setStatus(ResponseStatuses.error("Sql execution not supported"))
//                            .build();
//                    observer.onNext(resp);
//                    observer.onCompleted();
//                }
//
//                val executor = this.executionProvider
//                        .getSqlExecutionProvider();
//                val iterator = executor.execute(prepared.getSql(), request.getBatchSize());
//
//                streamResult(iterator, observer);
//                return;
//            }
//
//            val resp = ExecQueryResponse.newBuilder()
//                    .setStatus(ResponseStatuses.error("No Plan nor Sql provided"))
//                    .build();
//            observer.onNext(resp);
//            observer.onCompleted();
//        }
//        catch (Exception ex) {
//            val status = ResponseStatuses.error(ex);
//            observer.onNext(ExecQueryResponse.newBuilder()
//                    .setStatus(status)
//                    .build());
//            observer.onCompleted();
//        }
//    }
