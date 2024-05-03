package io.qpointz.delta.lineage.statements;

import io.qpointz.delta.lineage.LineageTable;
import io.qpointz.delta.lineage.model.LineageRepository;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.calcite.sql.SqlKind;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.ddl.SqlColumnDeclaration;
import org.apache.calcite.sql.ddl.SqlCreateTable;

@Slf4j
public class CreateTable implements Statement<SqlCreateTable> {

    public static CreateTable INSTANCE = new CreateTable();

    @Override
    public SqlKind kind() {
        return SqlKind.CREATE_TABLE;
    }

    @Override
    public void apply(SqlCreateTable statement, LineageRepository repository) {
        val columns = statement.columnList.stream()
                .map(k-> (SqlColumnDeclaration)k)
                .toList();

        val tbl = new LineageTable(statement.name, columns);
        repository.addTable(tbl);
    }

}
