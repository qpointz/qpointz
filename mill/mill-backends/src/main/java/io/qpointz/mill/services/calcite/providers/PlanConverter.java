package io.qpointz.mill.services.calcite.providers;

import io.substrait.plan.Plan;
import org.apache.calcite.rel.RelNode;

public interface PlanConverter {

    String toSql(Plan plan);

    String toSql(RelNode plan);

    RelNode toRelNode(Plan plan);

}
