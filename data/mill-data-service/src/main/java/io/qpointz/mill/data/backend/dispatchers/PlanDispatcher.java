package io.qpointz.mill.data.backend.dispatchers;

import java.io.IOException;

public interface PlanDispatcher {

    io.substrait.plan.Plan protoToPlan(io.substrait.proto.Plan plan) throws IOException;

    io.substrait.proto.Plan planToProto(io.substrait.plan.Plan plan) throws IOException;

    PlanHelper plan();


}
