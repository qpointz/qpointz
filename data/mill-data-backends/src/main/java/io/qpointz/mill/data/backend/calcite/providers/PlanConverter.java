package io.qpointz.mill.data.backend.calcite.providers;

import io.substrait.plan.Plan;
import org.apache.calcite.rel.RelNode;

import java.util.List;

public interface PlanConverter {

    record ConvertedPlanRelNode(RelNode node, List<String> names) {
    }

    record ConvertedPlanSql(String sql, List<String> names) {
    }

    ConvertedPlanSql toSql(Plan plan);

    ConvertedPlanSql toSql(RelNode plan);

    ConvertedPlanRelNode toRelNode(Plan plan);

}
