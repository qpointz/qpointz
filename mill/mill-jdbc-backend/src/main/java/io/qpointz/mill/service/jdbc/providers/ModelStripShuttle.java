package io.qpointz.mill.service.jdbc.providers;

import lombok.val;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.RelShuttle;
import org.apache.calcite.rel.core.TableFunctionScan;
import org.apache.calcite.rel.core.TableScan;
import org.apache.calcite.rel.logical.*;

public class ModelStripShuttle implements RelShuttle {

    @Override
    public RelNode visit(TableScan scan) {
        val table = scan.getTable();
        return null;
    }

    @Override
    public RelNode visit(TableFunctionScan scan) {
        return null;
    }

    @Override
    public RelNode visit(LogicalValues values) {
        return null;
    }

    @Override
    public RelNode visit(LogicalFilter filter) {
        return null;
    }

    @Override
    public RelNode visit(LogicalCalc calc) {
        return null;
    }

    @Override
    public RelNode visit(LogicalProject project) {
        return null;
    }

    @Override
    public RelNode visit(LogicalJoin join) {
        return null;
    }

    @Override
    public RelNode visit(LogicalCorrelate correlate) {
        return null;
    }

    @Override
    public RelNode visit(LogicalUnion union) {
        return null;
    }

    @Override
    public RelNode visit(LogicalIntersect intersect) {
        return null;
    }

    @Override
    public RelNode visit(LogicalMinus minus) {
        return null;
    }

    @Override
    public RelNode visit(LogicalAggregate aggregate) {
        return null;
    }

    @Override
    public RelNode visit(LogicalMatch match) {
        return null;
    }

    @Override
    public RelNode visit(LogicalSort sort) {
        return null;
    }

    @Override
    public RelNode visit(LogicalExchange exchange) {
        return null;
    }

    @Override
    public RelNode visit(LogicalTableModify modify) {
        return null;
    }

    @Override
    public RelNode visit(RelNode other) {
        return null;
    }
}
