package io.qpointz.mill.lineage.statements;

import io.qpointz.mill.lineage.LineageShuttle;
import io.qpointz.mill.lineage.SqlParse;
import io.qpointz.mill.lineage.model.LineageRepository;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.calcite.sql.SqlKind;
import org.apache.calcite.sql.SqlSelect;

@Slf4j
public class SelectStatement implements Statement<SqlSelect> {

    public static SelectStatement INSTANCE = new SelectStatement();

    @Override
    public SqlKind kind() {
        return SqlKind.SELECT;
    }

    @SneakyThrows
    @Override
    public void apply(SqlSelect statement, LineageRepository repository) {
        log.info("Select statement");
        val sqlstring = statement.toSqlString(repository.getDialect());
        val planner = repository.getPlanner();
        val parsed = planner.parse(sqlstring.getSql());
        val validated = planner.validate(parsed);
        val rel = planner.rel(validated).rel;
        val shuttle = new LineageShuttle();
        rel.accept(shuttle);
        val lin = shuttle.attributesOf(rel);
        repository.getReport().add(new SqlParse.LineageReportItem("QUERY", rel, sqlstring.getSql(), lin));
    }
}
