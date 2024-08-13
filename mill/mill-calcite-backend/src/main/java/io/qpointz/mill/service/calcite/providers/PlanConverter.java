package io.qpointz.mill.service.calcite.providers;

import io.substrait.plan.Plan;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.sql.SqlDialect;

public interface PlanConverter {

    String toSql(Plan plan);

    String toSql(RelNode plan);

    RelNode toRelNode(Plan plan);

}
