package io.qpointz.delta.service;

import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import io.qpointz.delta.proto.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

@Slf4j
@AllArgsConstructor
@GrpcService
public class DeltaServiceBase extends DeltaServiceGrpc.DeltaServiceImplBase {

    @Getter(AccessLevel.PROTECTED)
    private final SchemaProvider schemaProvider;

    @Getter(AccessLevel.PROTECTED)
    private final ExecutionProvider executionProvider;

    @Getter(AccessLevel.PROTECTED)
    private final SqlParserProvider sqlParserProvider;

    private boolean supportsSql() {
        return this.sqlParserProvider != null && sqlParserProvider.getAcceptsSql()
                && this.executionProvider!=null && this.executionProvider.canExecuteSql();
    }

    private boolean supportsSubstraitPlan() {
        return this.getExecutionProvider() != null && this.executionProvider.canExecuteSubstraitPlan();
    }

    @Override
    public void handshake(HandshakeRequest request, StreamObserver<HandshakeResponse> responseObserver) {
        process(request, responseObserver, this::onHandshake);
    }
    protected HandshakeResponse onHandshake(HandshakeRequest request) {
        val capabilities = HandshakeResponse.Capabilities.newBuilder()
                .setSupportSql(this.supportsSql())
                .setSupportPlan(this.supportsSubstraitPlan())
                .setSupportJsonPlan(false)
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
    public void listSchemas(ListSchemasRequest request, StreamObserver<ListSchemasResponse> responseObserver) {
        process(request, responseObserver, this::onListSchemas);
    }

    protected ListSchemasResponse onListSchemas(ListSchemasRequest listSchemasRequest) {
        return ListSchemasResponse.newBuilder()
                .setStatus(ResponseStatuses.ok())
                .addAllSchemas(this.getSchemaProvider().getSchemaNames())
                .build();
    }

    @Override
    public void getSchema(GetSchemaRequest request, StreamObserver<GetSchemaResponse> responseObserver) {
        process(request, responseObserver, this::onGetSchema);
    }

    protected GetSchemaResponse onGetSchema(GetSchemaRequest request) {
        val builder = GetSchemaResponse.newBuilder();
        val schema = this.getSchemaProvider().getSchema(request.getSchemaName());

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
    public void prepareStatement(PrepareStatementRequest request, StreamObserver<PrepareStatementResponse> responseObserver) {
        process(request, responseObserver, this::onPrepareStatement);
    }

    @AllArgsConstructor
    @Builder
    public static class PrepareResult {

        @Getter
        private Collection<String> errors;

        @Getter
        private PreparedStatement statement;

        public boolean isSuccess() {
            return errors==null || errors.size()==0;
        }

        public static PrepareResult of(Collection<String> errors) {
            return PrepareResult.builder()
                    .errors(errors)
                    .build();
        }

        public static PrepareResult of(String error) {
            return PrepareResult.of(List.of(error));
        }

        public static PrepareResult ok(PreparedStatement preparedStatement) {
            return PrepareResult.builder()
                    .errors(List.of())
                    .statement(preparedStatement)
                    .build();
        }
    }

    protected PrepareStatementResponse onPrepareStatement(PrepareStatementRequest request) {
        val statement = request.getStatement();

        val builder = PrepareStatementResponse.newBuilder()
                .setOriginalStatement(statement);

        PrepareResult preparedResult = prepareStatement(statement);

        if (preparedResult.isSuccess()) {
            return builder
                    .setStatus(ResponseStatuses.ok())
                    .setStatement(preparedResult.getStatement())
                    .build();
        } else {
            return builder
                    .setStatus(ResponseStatuses.error("Validation error", preparedResult.getErrors()))
                    .build();
        }

    }

    private PrepareResult prepareStatement(Statement statement) {
        if (statement.hasSql()) {
            return prepareAndValidateSql(statement.getSql());
        }

        if (statement.hasText()) {
            return prepareAndValidateText(statement.getText());
        }

        if (statement.hasPlan()) {
            return prepareAndValidatePlan(statement.getPlan());
        }

        return PrepareResult.of("Statement expected to provide Sql or Substrait or Text plan.");
    }

    private PrepareResult prepareAndValidatePlan(PlanStatement statement) {
        if (!this.supportsSubstraitPlan()) {
            return PrepareResult.of("Substrait plans not supported");
        }

        val preparedStatement = PreparedStatement.newBuilder()
                .setPlan(statement)
                .build();

        return PrepareResult.ok(preparedStatement);
    }

    private PrepareResult prepareAndValidateText(TextPlanStatement statement) {
        return PrepareResult.of("JSON plans not supported");
    }

    private PrepareResult prepareAndValidateSql(SQLStatement statement) {
        if (!this.supportsSql()) {
            return PrepareResult.of("SQL not supported");
        }

        val parseResult = this.getSqlParserProvider()
                .parse(statement.getStatement());

        val planStatement = PlanStatement.newBuilder()
                        .setPlan(parseResult.getPlan())
                .build();

        val preparedStatement = PreparedStatement.newBuilder()
                .setPlan(planStatement)
                .build();

        if (!parseResult.isSuccess()) {
            return PrepareResult.of(parseResult.getMessage());
        } else {
            return PrepareResult.ok(preparedStatement);
        }
    }

    @Override
    public void executeQueryStream(ExecQueryStreamRequest request, StreamObserver<ExecQueryResponse> respObserver) {
        var observer = (ServerCallStreamObserver<ExecQueryResponse>)respObserver;
        try {
            val prepared = request.getStatement();
            if (prepared.hasPlan()) {
                if (!this.executionProvider.canExecuteSubstraitPlan()) {
                    val resp = ExecQueryResponse.newBuilder()
                                    .setStatus(ResponseStatuses.error("Plan execution not suported"))
                                    .build();
                    observer.onNext(resp);
                    observer.onCompleted();
                }

                val executor = this.executionProvider
                        .getSubstraitExecutionProvider();
                val iterator = executor.execute(prepared.getPlan(), request.getBatchSize());

                streamResult(iterator, observer);
                return;
            }

            if (prepared.hasSql()) {
                if (!this.executionProvider.canExecuteSql()) {
                    val resp = ExecQueryResponse.newBuilder()
                            .setStatus(ResponseStatuses.error("Sql execution not supported"))
                            .build();
                    observer.onNext(resp);
                    observer.onCompleted();
                }

                val executor = this.executionProvider
                        .getSqlExecutionProvider();
                val iterator = executor.execute(prepared.getSql(), request.getBatchSize());

                streamResult(iterator, observer);
                return;
            }

            val resp = ExecQueryResponse.newBuilder()
                    .setStatus(ResponseStatuses.error("No Plan nor Sql provided"))
                    .build();
            observer.onNext(resp);
            observer.onCompleted();
        }
        catch (Exception ex) {
            val status = ResponseStatuses.error(ex);
            observer.onNext(ExecQueryResponse.newBuilder()
                            .setStatus(status)
                                    .build());
            observer.onCompleted();
        }
    }

    private void streamResult(Iterator<VectorBlock> iterator, ServerCallStreamObserver<ExecQueryResponse> observer) {
        while (iterator.hasNext()) {
            val resp = ExecQueryResponse.newBuilder()
                    .setStatus(ResponseStatuses.ok())
                    .setVector(iterator.next());
            observer.onNext(resp.build());
        }
        observer.onCompleted();
    }

    protected <TResp> void process(StreamObserver<TResp> observer, TResp reply) {
        if (reply!=null) {
            observer.onNext(reply);
            observer.onCompleted();
        } else {
            observer.onError(new NullPointerException("Null response"));
        }
    }

    protected <TReq, TResp> void process(TReq request, StreamObserver<TResp> observer, Function<TReq, TResp> onHandle) {
        try {
            final var reply = onHandle.apply(request);
            process(observer, reply);
        } catch (Exception ex) {
            log.error("Server error", ex);
            observer.onError(ex);
        }
    }
}
