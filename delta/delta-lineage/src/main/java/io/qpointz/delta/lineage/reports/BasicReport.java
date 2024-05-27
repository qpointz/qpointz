package io.qpointz.delta.lineage.reports;

import io.qpointz.delta.lineage.SqlParse;
import io.qpointz.delta.lineage.model.LineageRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;

@AllArgsConstructor
public class BasicReport implements Report {

    @Getter
    private final LineageRepository repository;

    public StringBuilder report() {
        val sb = new StringBuilder();
        for (val i : this.repository.getReport()) {
            report(i, sb);
        }
        return sb;
    }

    private void report(SqlParse.LineageReportItem i, StringBuilder sb) {
        sb.append(String.format("OBJECT:%s%n", i.name()));
        sb.append(String.format("SQL:%n%s%n", i.sql()));
        sb.append(String.format("PLAN:%n%s", i.plan().explain()));
        val rowType = i.plan().getRowType().getFieldList();
        sb.append("LINEAGE:\n");
        for (int idx=0;idx< rowType.size();idx++) {
            val name = rowType.get(idx).getName();
            val type = rowType.get(idx).getType();
            val attibs = i.lineage().get(idx);
            sb.append(String.format("\t%s (%s):\n", name, type.getSqlTypeName().toString()));
            for (val at: attibs) {
                sb.append(String.format("\t\t<-%s.%s\n", at.table().get(0), at.attribute()));
            }
        }
        sb.append("\n\n");
    }

}
