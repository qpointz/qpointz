package io.qpointz.mill.services;

import io.substrait.plan.Plan;

public interface PlanRewriter {
    Plan rewritePlan(Plan plan, PlanRewriteContext context);
}
