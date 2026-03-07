package io.qpointz.mill.data.backend.dispatchers;

import io.grpc.Status;
import io.qpointz.mill.data.backend.*;
import io.qpointz.mill.proto.*;
import io.qpointz.mill.sql.v2.dialect.DialectProtoMapper;
import io.qpointz.mill.sql.v2.dialect.DialectRegistry;
import io.qpointz.mill.vectors.VectorBlockIterator;
import io.substrait.plan.Plan;
import lombok.val;

import java.io.IOException;

public class DataOperationDispatcherImpl implements DataOperationDispatcher {
    private static final String DIALECT_SCHEMA_VERSION = "v1";
    private static final String DEFAULT_DIALECT_ID = "CALCITE";

    private final SchemaProvider schemaProvider;

    private final ExecutionProvider executionProvider;

    private final SqlProvider sqlProvider;

    private final SecurityDispatcher securityDispatcher;

    private final PlanRewriteChain planRewriteChain;

    private final SubstraitDispatcher substraitDispatcher;

    private final ResultAllocator resultAllocator;
    private final DialectRegistry dialectRegistry;
    private final String defaultDialectId;

    public DataOperationDispatcherImpl(SchemaProvider schemaProvider,
                                       ExecutionProvider executionProvider,
                                       SqlProvider sqlProvider,
                                       SecurityDispatcher securityDispatcher,
                                       PlanRewriteChain planRewriteChain,
                                       SubstraitDispatcher substrait,
                                       ResultAllocator resultAllocator) {
        this(schemaProvider,
                executionProvider,
                sqlProvider,
                securityDispatcher,
                planRewriteChain,
                substrait,
                resultAllocator,
                DialectRegistry.fromClasspathDefaults(),
                DEFAULT_DIALECT_ID);
    }

    public DataOperationDispatcherImpl(SchemaProvider schemaProvider,
                                       ExecutionProvider executionProvider,
                                       SqlProvider sqlProvider,
                                       SecurityDispatcher securityDispatcher,
                                       PlanRewriteChain planRewriteChain,
                                       SubstraitDispatcher substrait,
                                       ResultAllocator resultAllocator,
                                       DialectRegistry dialectRegistry,
                                       String defaultDialectId) {
        this.schemaProvider = schemaProvider;
        this.executionProvider = executionProvider;
        this.sqlProvider = sqlProvider;
        this.securityDispatcher = securityDispatcher;
        this.planRewriteChain = planRewriteChain;
        this.substraitDispatcher = substrait;
        this.resultAllocator = resultAllocator;
        this.dialectRegistry = dialectRegistry;
        this.defaultDialectId = defaultDialectId;

        if (this.dialectRegistry == null || this.dialectRegistry.size() == 0) {
            throw new IllegalStateException("Dialect registry is empty. Expected at least one configured dialect.");
        }
        if (this.defaultDialectId == null || this.defaultDialectId.isBlank()) {
            throw new IllegalStateException("Default dialect id is not configured.");
        }
        if (this.dialectRegistry.getDialect(this.defaultDialectId) == null) {
            throw new IllegalStateException(
                    String.format(
                            "Default dialect '%s' is missing from registry. Available: %s",
                            this.defaultDialectId,
                            String.join(",", this.dialectRegistry.ids())));
        }
    }


    private boolean supportsSql() {
        return this.sqlProvider != null && this.sqlProvider.supportsSql();
    }

    @Override
    public HandshakeResponse handshake(HandshakeRequest request) {
        val capabilities = HandshakeResponse.Capabilities.newBuilder()
                .setSupportSql(this.supportsSql())
                .setSupportDialect(this.dialectRegistry.size() > 0)
                .build();

        val auth = HandshakeResponse.AuthenticationContext.newBuilder()
                .setName(this.securityDispatcher.principalName())
                .build();

        return HandshakeResponse.newBuilder()
                .setVersion(ProtocolVersion.V1_0)
                .setCapabilities(capabilities)
                .setAuthentication(auth)
                .build();
    }

    @Override
    public GetDialectResponse getDialect(GetDialectRequest request) {
        final String requestedDialect = request.hasDialectId() && request.getDialectId() != null
                && !request.getDialectId().isBlank()
                ? request.getDialectId()
                : this.defaultDialectId;

        val spec = this.dialectRegistry.getDialect(requestedDialect);
        if (spec == null) {
            throw Status.NOT_FOUND
                    .augmentDescription(
                            String.format(
                                    "Dialect '%s' not found. Supported dialects: %s",
                                    requestedDialect,
                                    String.join(",", this.dialectRegistry.ids())))
                    .asRuntimeException();
        }

        return DialectProtoMapper.toResponse(spec, DIALECT_SCHEMA_VERSION);
    }

    @Override
    public ListSchemasResponse listSchemas(ListSchemasRequest listSchemasRequest) {
        return ListSchemasResponse.newBuilder()
                .addAllSchemas(this.schemaProvider.getSchemaNames())
                .build();
    }

    @Override
    public GetSchemaResponse getSchema(GetSchemaRequest r) {
        val builder = GetSchemaResponse.newBuilder();
        val requestedSchema = r.getSchemaName();

        if (!this.schemaProvider.isSchemaExists(requestedSchema)) {
            val message = String.format("Schema '%s' not found",
                    requestedSchema == null ? "<root>" : requestedSchema);
            throw Status.NOT_FOUND
                    .augmentDescription(message)
                    .asRuntimeException();
        }

        val schema = this.schemaProvider.getSchema(requestedSchema);
        return builder
                .setSchema(schema)
                .build();
    }

    @Override
    public ParseSqlResponse parseSql(ParseSqlRequest parseSqlRequest) {
        val parseResult = this.parseSqlResult(parseSqlRequest);
        if (parseResult.isSuccess())
            return ParseSqlResponse.newBuilder()
                    .setPlan(this.substraitDispatcher.planToProto(parseResult.getPlan()))
                    .build();
        throw parseResult.getException();
    }

    public SqlProvider.PlanParseResult parseSqlResult(ParseSqlRequest parseSqlRequest) {
        if (!this.supportsSql()) {
            return SqlProvider.PlanParseResult.fail(Status.UNIMPLEMENTED
                    .augmentDescription("SQL not supported")
                    .asRuntimeException());
        }
        val parseResult = this.sqlProvider.parseSql(parseSqlRequest.getStatement().getSql());
        if (parseResult.isSuccess()) {
            return parseResult;
        }

        val ex = Status.INVALID_ARGUMENT
                .augmentDescription(parseResult.getMessage())
                .asRuntimeException();

        return SqlProvider.PlanParseResult.fail(ex);
    }

    @Override
    public QueryResultResponse submitQuery(QueryRequest queryRequest) {
        val iterator = this.execute(queryRequest);
        val allocationResult = this.resultAllocator.allocate(iterator);
        return fetchResult(QueryResultRequest.newBuilder()
                .setPagingId(allocationResult.pagingId())
                .build());
    }

    @Override
    public VectorBlockIterator execute(QueryRequest request) {
        Plan plan;
        if (request.hasStatement()) {
            val parseQueryRequest = ParseSqlRequest.newBuilder()
                    .setStatement(request.getStatement())
                    .build();

            val parseResult = this.parseSqlResult(parseQueryRequest);
            if (!parseResult.isSuccess()) {
                throw parseResult.getException();
            }
            plan = parseResult.getPlan();
        } else {
            try {
                plan = this.substraitDispatcher.protoToPlan(request.getPlan());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        val config = request.getConfig();
        return this.execute(plan, config);
    }

    public VectorBlockIterator execute(Plan plan, QueryExecutionConfig config) {
        val rewrittenPlan = this.rewritePlan(plan);
        return this.executionProvider.execute(rewrittenPlan,config);
    }

    private boolean hasPlanRewritesChain() {
        return this.planRewriteChain != null && !this.planRewriteChain.getRewriters().isEmpty();
    }

    public final class PlanRewriteContextImpl implements PlanRewriteContext {

    }

    private Plan rewritePlan(Plan originalPlan) {
        if (!this.hasPlanRewritesChain()) {
            return originalPlan;
        }

        val rewriteContext = new PlanRewriteContextImpl();
        return this.planRewriteChain.rewrite(originalPlan, rewriteContext);
    }

    public QueryResultResponse fetchResult(QueryResultRequest request) {
        val key = request.getPagingId();
        val fetchResult = this.resultAllocator.nextBlock(key);
        val builder = QueryResultResponse.newBuilder();
        if (!fetchResult.exists()) {
            return  builder.build();
        }

        if (fetchResult.nextPagingId()!=null && !fetchResult.nextPagingId().isEmpty()) {
            builder.setPagingId(fetchResult.nextPagingId());
        }

        return builder
                .setVector(fetchResult.block())
                .build();
    }

}
