package io.qpointz.mill.services.dispatchers;

import io.grpc.Status;
import io.qpointz.mill.proto.*;
import io.qpointz.mill.services.*;
import io.qpointz.mill.vectors.VectorBlockIterator;
import io.substrait.plan.Plan;
import lombok.val;

import java.io.IOException;

public class DataOperationDispatcherImpl implements DataOperationDispatcher {

    private final SchemaProvider schemaProvider;

    private final ExecutionProvider executionProvider;

    private final SqlProvider sqlProvider;

    private final SecurityDispatcher securityDispatcher;

    private final PlanRewriteChain planRewriteChain;

    private final SubstraitDispatcher substraitDispatcher;

    private final ResultAllocator resultAllocator;

    public DataOperationDispatcherImpl(SchemaProvider schemaProvider,
                                       ExecutionProvider executionProvider,
                                       SqlProvider sqlProvider,
                                       SecurityDispatcher securityDispatcher,
                                       PlanRewriteChain planRewriteChain,
                                       SubstraitDispatcher substrait,
                                       ResultAllocator resultAllocator) {
        this.schemaProvider = schemaProvider;
        this.executionProvider = executionProvider;
        this.sqlProvider = sqlProvider;
        this.securityDispatcher = securityDispatcher;
        this.planRewriteChain = planRewriteChain;
        this.substraitDispatcher = substrait;
        this.resultAllocator = resultAllocator;
    }


    private boolean supportsSql() {
        return this.sqlProvider != null && this.sqlProvider.supportsSql();
    }

    @Override
    public HandshakeResponse handshake(HandshakeRequest request) {
        val capabilities = HandshakeResponse.Capabilities.newBuilder()
                .setSupportSql(this.supportsSql())
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
