package io.qpointz.mill.service;

import io.substrait.plan.Plan;

public interface PlanRewriter {
    Plan rewritePlan(Plan plan);
}
