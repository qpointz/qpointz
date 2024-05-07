package io.qpointz.delta.lineage;

import lombok.val;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.RelShuttle;
import org.apache.calcite.rel.core.TableFunctionScan;
import org.apache.calcite.rel.core.TableScan;
import org.apache.calcite.rel.logical.*;
import org.apache.calcite.rex.*;

import java.util.*;

public class LineageShuttle implements RelShuttle, RexVisitor<Set<Integer>> {

    private LineageItems<RelNode> items = new LineageItems<>();

    public record RelNodeLineage(RelNode node, ArrayList<Set<LineageItems.TableAttribute>> attributes, ArrayList<Set<LineageItems.TableAttribute>> used) {

        public Set<LineageItems.TableAttribute> flatAttributes() {
            val res = new HashSet<LineageItems.TableAttribute>();
            this.attributes().stream()
                    .forEach(z -> res.addAll(z));
            return res;
        }

        public Set<LineageItems.TableAttribute> flatUsed() {
            val res = new HashSet<LineageItems.TableAttribute>();
            this.used().stream()
                    .forEach(z -> res.addAll(z));
            return res;
        }

    }

    public static RelNodeLineage extract(RelNode rel) {
        val ls = new LineageShuttle();
        rel.accept(ls);
        val attributes = ls.attributesOf(rel);
        val used =  ls.attributesUsed(rel);
        return new RelNodeLineage(rel, attributes, used);
    }

    public ArrayList<Set<LineageItems.TableAttribute>> attributesOf(RelNode rel) {
        var res = new ArrayList<Set<LineageItems.TableAttribute >>();
        for (var idx=0;idx < rel.getRowType().getFieldCount();idx++) {
            res.add(items.attributesOf(rel, idx));
        }
        return res;
    }

    public ArrayList<Set<LineageItems.TableAttribute>> attributesUsed(RelNode rel) {
        var res = new ArrayList<Set<LineageItems.TableAttribute >>();
        for (var idx=0;idx < rel.getRowType().getFieldCount();idx++) {
            res.add(items.usedBy(rel));
        }
        return res;
    }

    @Override
    public RelNode visit(TableScan scan) {
        val tableName = scan.getTable().getQualifiedName();
        val rowType = scan.getRowType();
        val names = rowType.getFieldNames();
        for (int i=0;i< rowType.getFieldCount(); i++) {
            items.addAttributes(scan, i, tableName, names.get(i));
        }
        return scan;
    }

    @Override
    public RelNode visit(LogicalProject project) {
        val input = project.getInput();
        input.accept(this);
        val projects = project.getProjects();
        val cnt = projects.size();
        for (var idx=0;idx < cnt;idx++) {
            val proj = projects.get(idx);
            val indexes = proj.accept(this);
            items.add(project, idx, input, indexes);
        }
        return project;
    }

    @Override
    public RelNode visit(TableFunctionScan scan) {
        throw new RuntimeException("Not supported yet.");
    }

    @Override
    public RelNode visit(LogicalValues values) {
        throw new RuntimeException("Not supported yet.");
    }

    @Override
    public RelNode visit(LogicalFilter filter) {
        val input = filter.getInput();
        input.accept(this);
        val conditionIndexes = filter.getCondition().accept(this);
        for (var idx=0;idx < filter.getRowType().getFieldCount();idx++) {
            items.add(filter, idx, input, idx);
        }
        items.addUsed(filter, 0, input, conditionIndexes);
        return filter;
    }

    @Override
    public RelNode visit(LogicalCalc calc) {
        throw new RuntimeException("Not supported yet.");
    }

    @Override
    public RelNode visit(LogicalJoin join) {
        val left = join.getLeft();
        val right = join.getRight();
        left.accept(this);
        right.accept(this);

        val leftSize = left.getRowType().getFieldCount();

        for (var idx=0; idx < leftSize;idx++) {
            items.add(join, idx, left, idx);
        }

        for (var idx=0; idx < right.getRowType().getFieldCount();idx++) {
            items.add(join, idx + leftSize, right, idx);
        }

        val conditionIdx = join.getCondition().accept(this);
        for (val idx : conditionIdx) {
            if (idx < leftSize) {
                items.addUsed(join, -1, left, idx);
            } else {
                items.addUsed(join, -1 , right, idx - leftSize);
            }

        }

        return join;


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
