package io.qpointz.mill.data.backend;


import io.qpointz.mill.MillRuntimeException;
import io.substrait.plan.Plan;
import io.substrait.proto.AdvancedExtension;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

@AllArgsConstructor
public final class PlanRewriteChain {

    @Getter
    private final List<PlanRewriter> rewriters;

    public Plan rewrite(Plan plan, PlanRewriteContext rewriteContext) {
        if (this.getRewriters()==null || this.getRewriters().isEmpty()) {
            return plan;
        }

        var rewrittenPlan = plan;
        for (val rewriter : this.getRewriters()) {
            rewrittenPlan = rewriter.rewritePlan(rewrittenPlan, rewriteContext);
        }


        val originalRoots = plan.getRoots();
        var rewrittenRoots = rewrittenPlan.getRoots();

        if (originalRoots.size()!= rewrittenRoots.size()) {
            throw new MillRuntimeException("Rewrite error. Mismatching roots");
        }

        val finalRoots = IntStream.range(0, originalRoots.size())
                .mapToObj(k-> Plan.Root.builder()
                        .from(rewrittenRoots.get(k))
                        .addAllNames(originalRoots.get(k).getNames())
                        .build())
                .toList();

        val finalPlanBuilder = io.substrait.plan.ImmutablePlan.builder()
                .addAllRoots(finalRoots)
                .version(rewrittenPlan.getVersion())
                .addAllExpectedTypeUrls(rewrittenPlan.getExpectedTypeUrls());

        if (rewrittenPlan.getAdvancedExtension().isPresent()) {
            finalPlanBuilder.advancedExtension(rewrittenPlan.getAdvancedExtension());
        }

        return finalPlanBuilder
                .build();
    }
}
