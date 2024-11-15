package io.qpointz.mill.services.dispatchers;

import io.qpointz.mill.services.MetadataProvider;
import io.substrait.extension.SimpleExtension;
import io.substrait.proto.Plan;

import java.io.IOException;

public class PlanDispatcherImpl implements PlanDispatcher {

    private final SimpleExtension.ExtensionCollection extensionCollection;
    private final PlanHelper planHelper;
    private final MetadataProvider metadataProvider;

    public PlanDispatcherImpl(SimpleExtension.ExtensionCollection extensionCollection, MetadataProvider metadataProvider) {
        this.extensionCollection = extensionCollection;
        this.metadataProvider = metadataProvider;
        this.planHelper = new PlanHelper(this.metadataProvider, extensionCollection);
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
