package io.qpointz.delta.lineage.statements;

import io.qpointz.delta.lineage.model.LineageRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.calcite.sql.SqlKind;
import org.apache.calcite.sql.SqlNode;

@Slf4j
public class NopeStatement implements Statement<SqlNode> {

    private final SqlNode node;

    private NopeStatement(SqlNode node) {
        this.node = node;
    }

    public static NopeStatement createNopeStatement(SqlNode node) {
        return new NopeStatement(node);
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
