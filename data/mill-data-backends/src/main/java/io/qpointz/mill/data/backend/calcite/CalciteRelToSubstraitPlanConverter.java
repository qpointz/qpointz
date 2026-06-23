package io.qpointz.mill.data.backend.calcite;

import io.qpointz.mill.data.backend.dispatchers.SubstraitDispatcher;
import io.substrait.isthmus.SubstraitRelVisitor;
import io.substrait.plan.Plan;
import lombok.RequiredArgsConstructor;
import org.apache.calcite.rel.RelRoot;

/**
 * Calcite implementation of {@link RelToSubstraitPlanConverter} using {@link SubstraitRelVisitor}.
 */
@RequiredArgsConstructor
public class CalciteRelToSubstraitPlanConverter implements RelToSubstraitPlanConverter {

    private final SubstraitDispatcher substraitDispatcher;

    @Override
    public Plan convert(RelRoot relRoot) {
        var root = SubstraitRelVisitor.convert(relRoot, substraitDispatcher.getExtensionCollection());
        return Plan.builder().addRoots(root).build();
    }
}
