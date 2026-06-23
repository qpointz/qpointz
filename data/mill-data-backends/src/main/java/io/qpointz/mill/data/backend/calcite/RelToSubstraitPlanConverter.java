package io.qpointz.mill.data.backend.calcite;

import io.substrait.plan.Plan;
import org.apache.calcite.tools.RelBuilder;

/**
 * Converts composed Calcite {@link org.apache.calcite.rel.RelRoot} trees to Substrait {@link Plan} instances.
 */
public interface RelToSubstraitPlanConverter {

    /**
     * @param relRoot composed relational plan root
     * @return Substrait plan ready for dispatcher execution
     */
    Plan convert(org.apache.calcite.rel.RelRoot relRoot);
}
