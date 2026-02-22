package io.qpointz.mill.data.backend;

import io.substrait.plan.Plan;

public interface PlanRewriter {
    Plan rewritePlan(Plan plan, PlanRewriteContext context);
}
