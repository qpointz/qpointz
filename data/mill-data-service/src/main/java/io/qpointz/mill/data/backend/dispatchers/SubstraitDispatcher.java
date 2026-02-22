package io.qpointz.mill.data.backend.dispatchers;

import io.substrait.dsl.SubstraitBuilder;
import io.substrait.extension.SimpleExtension;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.IOException;

@AllArgsConstructor
public class SubstraitDispatcher {

    @Getter
    private final SimpleExtension.ExtensionCollection extensionCollection;

    public SubstraitBuilder newSubstraitBuilder() {
        return new SubstraitBuilder(this.extensionCollection);
    }

    public io.substrait.plan.Plan protoToPlan(io.substrait.proto.Plan plan) throws IOException {
        return new io.substrait.plan.ProtoPlanConverter().from(plan);
    }

    public io.substrait.proto.Plan planToProto(io.substrait.plan.Plan plan) {
        return new io.substrait.plan.PlanProtoConverter().toProto(plan);
    }

}
