package io.qpointz.delta.lineage;

import lombok.val;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.RelShuttle;
import org.apache.calcite.rel.core.TableFunctionScan;
import org.apache.calcite.rel.core.TableScan;
import org.apache.calcite.rel.logical.*;
import org.apache.calcite.rex.*;
import org.apache.calcite.sql.SqlIdentifier;

import java.util.*;

public class LineageShuttle implements RelShuttle, RexVisitor<Set<Integer>> {

    private LineageItems<RelNode> items = new LineageItems<>();

    @Override
    public RelNode visit(TableScan scan) {
        val tableName = scan.getTable().getQualifiedName();
        val rowType = scan.getRowType();
        val names = rowType.getFieldNames();
        for (int i=0;i< rowType.getFieldCount(); i++) {
            items.add(scan, i, null, -1, tableName, names.get(i));
        }
        return scan;
    }

    @Override
    public RelNode visit(LogicalProject project) {
        project.getInput().accept(this);
        val
        for (val p: project.getProjects()) {
            val indexes = p.accept(this);
            for (val idx :  )
        }
        return project;
    }

    @Override
    public RelNode visit(TableFunctionScan scan) {
        throw new RuntimeException("Not supported yet.");
    }

    @Override
    public RelNode visit(LogicalValues values) {
        return null;
    }

    @Override
    public RelNode visit(LogicalFilter filter) {
        throw new RuntimeException("Not supported yet.");
    }

    @Override
    public RelNode visit(LogicalCalc calc) {
        throw new RuntimeException("Not supported yet.");
    }

    @Override
    public RelNode visit(LogicalJoin join) {
        throw new RuntimeException("Not supported yet.");
    }

    @Override
    public RelNode visit(LogicalCorrelate correlate) {
        throw new RuntimeException("Not supported yet.");
    }

    @Override
    public RelNode visit(LogicalUnion union) {
        throw new RuntimeException("Not supported yet.");
    }

    @Override
    public RelNode visit(LogicalIntersect intersect) {
        throw new RuntimeException("Not supported yet.");
    }

    @Override
    public RelNode visit(LogicalMinus minus) {
        throw new RuntimeException("Not supported yet.");
    }

    @Override
    public RelNode visit(LogicalAggregate aggregate) {
        throw new RuntimeException("Not supported yet.");
    }

    @Override
    public RelNode visit(LogicalMatch match) {
        throw new RuntimeException("Not supported yet.");
    }

    @Override
    public RelNode visit(LogicalSort sort) {
        throw new RuntimeException("Not supported yet.");
    }

    @Override
    public RelNode visit(LogicalExchange exchange) {
        throw new RuntimeException("Not supported yet.");
    }

    @Override
    public RelNode visit(LogicalTableModify modify) {
        throw new RuntimeException("Not supported yet.");
    }

    @Override
    public RelNode visit(RelNode other) {
        throw new RuntimeException("Not supported yet.");
    }

    @Override
    public Set<Integer> visitInputRef(RexInputRef inputRef) {
        return Set.of(inputRef.getIndex());
    }

    @Override
    public Set<Integer> visitLocalRef(RexLocalRef localRef) {
        return Set.of();
    }

    @Override
    public Set<Integer> visitLiteral(RexLiteral literal) {
        return Set.of();
    }

    @Override
    public Set<Integer> visitCall(RexCall call) {
        val res = new HashSet<Integer>();
        for(val o : call.getOperands()) {
            res.addAll(o.accept(this));
        }
        return res;
    }

    @Override
    public Set<Integer> visitOver(RexOver over) {
        throw new RuntimeException("Not supported yet.");
    }

    @Override
    public Set<Integer> visitCorrelVariable(RexCorrelVariable correlVariable) {
        throw new RuntimeException("Not supported yet.");
    }

    @Override
    public Set<Integer> visitDynamicParam(RexDynamicParam dynamicParam) {
        return Set.of();
    }

    @Override
    public Set<Integer> visitRangeRef(RexRangeRef rangeRef) {
        throw new RuntimeException("Not supported yet.");
    }

    @Override
    public Set<Integer> visitFieldAccess(RexFieldAccess fieldAccess) {
        throw new RuntimeException("Not supported yet.");
    }

    @Override
    public Set<Integer> visitSubQuery(RexSubQuery subQuery) {
        throw new RuntimeException("Not supported yet.");
    }

    @Override
    public Set<Integer> visitTableInputRef(RexTableInputRef fieldRef) {
        throw new RuntimeException("Not supported yet.");
    }

    @Override
    public Set<Integer> visitPatternFieldRef(RexPatternFieldRef fieldRef) {
        throw new RuntimeException("Not supported yet.");
    }
}
