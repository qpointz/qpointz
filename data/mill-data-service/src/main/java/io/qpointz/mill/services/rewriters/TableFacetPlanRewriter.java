package io.qpointz.mill.services.rewriters;

import io.qpointz.mill.services.PlanRewriter;
import io.qpointz.mill.services.PlanRewriteContext;
import io.qpointz.mill.services.dispatchers.SubstraitDispatcher;
import io.substrait.plan.Plan;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.val;

public class TableFacetPlanRewriter implements PlanRewriter {

    @Getter(AccessLevel.PROTECTED)
    private final TableFacetFactory facetsFactory;

    @Getter(AccessLevel.PROTECTED)
    private final SubstraitDispatcher substraitDispatcher;

    public TableFacetPlanRewriter(TableFacetFactory facetsFactory, SubstraitDispatcher substraitDispatcher) {
        this.facetsFactory = facetsFactory;
        this.substraitDispatcher = substraitDispatcher;
    }

    @Override
    public Plan rewritePlan(Plan plan, PlanRewriteContext context) {
        val planBuilder = io.substrait.plan.ImmutablePlan.builder();
        val roots = plan.getRoots();
        val facetVisitor = TableFacetVisitor.builder()
                .facets(this.facetsFactory.facets())
                .extensionCollection(this.substraitDispatcher.getExtensionCollection())
                .build();

        for (val root : roots) {
            try {
                val newInput = root.getInput().accept(facetVisitor);
                if (newInput.isEmpty()) {
                    continue;
                }
                val newRootBuilder = io.substrait.plan.ImmutableRoot.builder();
                val newRoot = newRootBuilder
                        .input(newInput.get())
                        .build();
                planBuilder.addRoots(newRoot);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return planBuilder
                .build();
    }


}
