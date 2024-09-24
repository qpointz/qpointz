package io.qpointz.mill.services;

import io.qpointz.mill.proto.QueryExecutionConfig;
import io.qpointz.mill.proto.Schema;
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
        return this.getSqlProvider().parseSql(sql);
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
}
