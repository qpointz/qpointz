package io.qpointz.mill.lineage.statements;

import io.qpointz.mill.lineage.model.LineageRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.calcite.sql.SqlKind;
import org.apache.calcite.sql.SqlNode;

@Slf4j
public class NoopStatement implements Statement<SqlNode> {

    private final SqlNode node;

    private NoopStatement(SqlNode node) {
        this.node = node;
    }

    public static NoopStatement createNopeStatement(SqlNode node) {
        return new NoopStatement(node);
    }

    @Override
    public SqlKind kind() {
        return this.node.getKind();
    }

    @Override
    public void apply(SqlNode statement, LineageRepository repository) {
        log.warn("{} statements not supported", this.kind());
        log.warn(this.node.toString());
    }
}
