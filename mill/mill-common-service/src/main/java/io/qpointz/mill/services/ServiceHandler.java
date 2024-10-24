package io.qpointz.mill.services;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.grpc.Status;
import io.qpointz.mill.proto.*;
import io.qpointz.mill.services.utils.SubstraitUtils;
import io.qpointz.mill.vectors.VectorBlockIterator;
import io.substrait.plan.Plan;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.val;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

public class ServiceHandler {

    @Getter
    private final MetadataProvider metadataProvider;

    @Getter
    private final ExecutionProvider executionProvider;

    @Getter
    private final SqlProvider sqlProvider;

    @Getter
    private final SecurityProvider securityProvider;

    @Getter
    private final PlanRewriteChain planRewriteChain;

    @Getter(lazy = true)
    private final Cache<String, VectorBlockIterator> submitCache = createCache();

    public ServiceHandler(MetadataProvider metadataProvider, ExecutionProvider executionProvider, SqlProvider sqlProvider, SecurityProvider securityProvider, PlanRewriteChain planRewriteChain) {
        this.metadataProvider = metadataProvider;
        this.executionProvider = executionProvider;
        this.sqlProvider = sqlProvider;
        this.securityProvider = securityProvider !=null ? securityProvider : new NoneSecurityProvider();
        this.planRewriteChain = planRewriteChain;
    }

    private Cache<String, VectorBlockIterator> createCache() {
        return CacheBuilder.newBuilder()
                .expireAfterAccess(10, TimeUnit.MINUTES)
                .build();
    }

    private boolean hasPlanRewritesChain() {
        return this.planRewriteChain != null && !this.planRewriteChain.getRewriters().isEmpty();
    }

    private boolean supportsSql() {
        return this.getSqlProvider() != null;
    }

    private Iterable<String> getSchemaNames() {
        return this.getMetadataProvider().getSchemaNames();
    }

    public boolean schemaExists(String schemaName) {
        return this.getMetadataProvider().isSchemaExists(schemaName);
    }


    public SqlProvider.ParseResult parseSql(String sql) {
        if (!this.supportsSql()) {
            return SqlProvider.ParseResult.fail(Status.UNIMPLEMENTED
                    .augmentDescription("SQL not supported")
                    .asRuntimeException());
        }

        val parseResult = this.getSqlProvider().parseSql(sql);
        if (parseResult.isSuccess()) {
            return parseResult;
        }

        val ex = Status.INVALID_ARGUMENT
                .augmentDescription(parseResult.getMessage())
                .asRuntimeException();

        return SqlProvider.ParseResult.fail(ex);
    }

    private Plan rewritePlan(Plan originalPlan) {
        if (!this.hasPlanRewritesChain()) {
            return originalPlan;
        }
        var plan = originalPlan;
        for (val rewriter : this.planRewriteChain.getRewriters()) {
            plan = rewriter.rewritePlan(plan);
        }
        return plan;
    }

    public VectorBlockIterator execute(Plan plan, QueryExecutionConfig config) {
        val rewrittenPlan = this.rewritePlan(plan);
        return this.executionProvider.execute(rewrittenPlan,config);
    }

    public String getPrincipalName() {
        return this.getSecurityProvider().getPrincipalName();
    }

    public HandshakeResponse handshake() {
        val capabilities = HandshakeResponse.Capabilities.newBuilder()
                .setSupportSql(this.supportsSql())
                .build();

        val auth = HandshakeResponse.AuthenticationContext.newBuilder()
                .setName(this.getPrincipalName())
                .build();

        return HandshakeResponse.newBuilder()
                .setVersion(ProtocolVersion.V1_0)
                .setCapabilities(capabilities)
                .setAuthentication(auth)
                .build();
    }

    public ListSchemasResponse listSchemas() {
        return ListSchemasResponse.newBuilder()
                .addAllSchemas(this.getSchemaNames())
                .build();
    }

    public GetSchemaResponse getSchemaProto(GetSchemaRequest r) {
        val builder = GetSchemaResponse.newBuilder();
        val requestedSchema = r.getSchemaName();
        val schema = this.getMetadataProvider().getSchema(requestedSchema);

        if (!schemaExists(r.getSchemaName())) {
            val message = String.format("Schema '%s' not found",
                    requestedSchema == null ? "<root>" : requestedSchema);
            throw Status.NOT_FOUND
                    .augmentDescription(message)
                    .asRuntimeException();
        }
        return builder
                .setSchema(schema)
                .build();
    }

    @SneakyThrows
    public ParseSqlResponse parseSqlProto(ParseSqlRequest request) {
        val parseResult = parseSql(request.getStatement().getSql());
        if (parseResult.isSuccess())
            return ParseSqlResponse.newBuilder()
                    .setPlan(SubstraitUtils.planToProto(parseResult.getPlan()))
                    .build();
        throw parseResult.getException();
    }

    @SneakyThrows
    protected io.substrait.plan.Plan convertProtoToPlan(io.substrait.proto.Plan plan) {
        return SubstraitUtils.protoToPlan(plan);
    }

    public VectorBlockIterator executeToIterator(QueryRequest request) {
        Plan plan;
        if (request.hasStatement()) {
            val sqlStatement = request.getStatement();
            val parseResult = this.parseSql(sqlStatement.getSql());
            if (!parseResult.isSuccess()) {
                throw parseResult.getException();
            }
            plan = parseResult.getPlan();
        } else {
            plan = convertProtoToPlan(request.getPlan());
        }
        val config = request.getConfig();
        return this.execute(plan, config);
    }

    private String newKey() {
        val random = new SecureRandom();
        val bytes = new byte[32];
        random.nextBytes(bytes);
        return String.valueOf(Base64.getEncoder().encode(bytes));
    }

    public QueryResultResponse submitQuery(QueryRequest request) {
        val iterator = this.executeToIterator(request);
        val key = newKey();
        this.getSubmitCache().put(key, iterator);
        return fetchResult(QueryResultRequest.newBuilder().setPagingId(key).build());
    }

    public QueryResultResponse fetchResult(QueryResultRequest request) {
        val key = request.getPagingId();
        val cache = this.getSubmitCache();
        VectorBlockIterator iter;
        val builder = QueryResultResponse.newBuilder();
        synchronized (cache) {
            iter = cache.getIfPresent(key);
            if (iter == null) {
                return builder.build();
            }
            cache.invalidate(key);
        }

        if (iter.hasNext()) {
            val nextKey = newKey();
            val vector = iter.next();
            builder.setVector(vector);
            if (vector.getVectorSize() > 0) {
                cache.put(nextKey, iter);
                builder.setPagingId(nextKey);
            }
        }

        return builder
                .build();

    }

}
