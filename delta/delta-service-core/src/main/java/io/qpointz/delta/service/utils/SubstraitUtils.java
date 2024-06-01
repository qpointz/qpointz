package io.qpointz.delta.service.utils;

import java.io.IOException;

public class SubstraitUtils {

    private SubstraitUtils() {}

    public static io.substrait.plan.Plan protoToPlan(io.substrait.proto.Plan plan) throws IOException {
        return new io.substrait.plan.ProtoPlanConverter().from(plan);
    }

    public static io.substrait.proto.Plan planToProto(io.substrait.plan.Plan plan) {
        return new io.substrait.plan.PlanProtoConverter().toProto(plan);
    }

}
