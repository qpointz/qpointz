package io.qpointz.delta.service;

import io.qpointz.delta.proto.QueryExecutionConfig;
import io.qpointz.delta.sql.VectorBlockIterator;
import io.substrait.plan.Plan;

import java.io.IOException;

public interface ExecutionProvider {

    VectorBlockIterator execute(Plan plan, QueryExecutionConfig config);

    default io.substrait.plan.Plan protoToPlan(io.substrait.proto.Plan plan) throws IOException {
        return new io.substrait.plan.ProtoPlanConverter().from(plan);
    }

    default io.substrait.proto.Plan planToProto(io.substrait.plan.Plan plan) throws IOException {
        return new io.substrait.plan.PlanProtoConverter().toProto(plan);
    }
}
