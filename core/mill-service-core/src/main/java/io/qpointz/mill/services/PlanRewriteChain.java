package io.qpointz.mill.services;


import io.substrait.plan.Plan;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;

import java.util.List;

@AllArgsConstructor
public final class PlanRewriteChain {

    @Getter
    private final List<PlanRewriter> rewriters;

    public Plan rewrite(Plan plan, PlanRewriteContext rewriteContext) {
        if (this.getRewriters()==null || this.getRewriters().isEmpty()) {
            return plan;
        }

        var rewrittenPlan = plan;
        for (val rewriter : this.getRewriters()) {
            rewrittenPlan = rewriter.rewritePlan(rewrittenPlan, rewriteContext);
        }
        return rewrittenPlan;
    }
}
