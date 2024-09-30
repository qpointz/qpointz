package io.qpointz.mill.services;

import io.grpc.Status;
import io.qpointz.mill.proto.*;
import io.qpointz.mill.services.utils.SubstraitUtils;
import io.qpointz.mill.vectors.VectorBlockIterator;
import io.substrait.plan.Plan;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;

@AllArgsConstructor
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

    private boolean hasPlanRewritesChain() {
        return this.planRewriteChain != null && !this.planRewriteChain.getRewriters().isEmpty();
    }

    public boolean supportsSql() {
        return this.sqlProvider != null;
    }

    public Iterable<String> getSchemaNames() {
        return this.getMetadataProvider().getSchemaNames();
    }

    public Schema getSchema(String schemaName) {
        return this.getMetadataProvider().getSchema(schemaName);
    }

    public boolean schemaExists(String schemaName) {
        return this.getMetadataProvider().isSchemaExists(schemaName);
    }


    public SqlProvider.ParseResult parseSql(String sql) {
        if (!this.supportsSql()) {
            throw Status.UNIMPLEMENTED
                    .augmentDescription("SQL not supported")
                    .asRuntimeException();
        }

        val parseResult = this.getSqlProvider().parseSql(sql);

        if (!parseResult.isSuccess()) {
            throw Status.INVALID_ARGUMENT
                    .augmentDescription(parseResult.getMessage())
                    .withCause(parseResult.getException())
                    .asRuntimeException();
        }

        return parseResult;
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

    public HandshakeResponse handshake(HandshakeRequest request) {
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

    public ListSchemasResponse listSchemas(ListSchemasRequest request) {
        return ListSchemasResponse.newBuilder()
                .addAllSchemas(this.getSchemaNames())
                .build();
    }

    public GetSchemaResponse getSchemaProto(GetSchemaRequest r) {
        val builder = GetSchemaResponse.newBuilder();
        val requestedSchema = r.getSchemaName();
        val schema = this.getSchema(requestedSchema);

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

    public ParseSqlResponse parseSqlProto(ParseSqlRequest request) {
        val parseResult = parseSql(request.getStatement().getSql());
        return ParseSqlResponse.newBuilder()
                .setPlan(SubstraitUtils.planToProto(parseResult.getPlan()))
                .build();
    }
}
