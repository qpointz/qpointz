package io.qpointz.mill.data.backend.dispatchers;

import io.qpointz.mill.data.backend.SchemaProvider;
import io.substrait.extension.SimpleExtension;
import io.substrait.proto.Plan;

import java.io.IOException;

public class PlanDispatcherImpl implements PlanDispatcher {

    private final SimpleExtension.ExtensionCollection extensionCollection;
    private final PlanHelper planHelper;
    private final SchemaProvider schemaProvider;

    public PlanDispatcherImpl(SimpleExtension.ExtensionCollection extensionCollection, SchemaProvider schemaProvider) {
        this.extensionCollection = extensionCollection;
        this.schemaProvider = schemaProvider;
        this.planHelper = new PlanHelper(this.schemaProvider, extensionCollection);
    }

    @Override
    public io.substrait.plan.Plan protoToPlan(Plan plan) throws IOException {
        return new io.substrait.plan.ProtoPlanConverter().from(plan);
    }

    @Override
    public Plan planToProto(io.substrait.plan.Plan plan) throws IOException {
        return new io.substrait.plan.PlanProtoConverter().toProto(plan);
    }

    @Override
    public PlanHelper plan() {
        return this.planHelper;
    }


}
