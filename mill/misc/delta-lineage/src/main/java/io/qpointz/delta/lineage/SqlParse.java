package io.qpointz.mill.lineage;

import io.qpointz.mill.lineage.model.LineageRepository;
import io.qpointz.mill.lineage.statements.Statements;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.sql.*;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.*;
import java.util.function.Consumer;

@Slf4j
public abstract class SqlParse {

    private LineageRepository repository;

    protected SqlParse(SqlDialect.DatabaseProduct product) {
        this.repository = new LineageRepository(product);
    }

    protected abstract Iterator<Reader> getReaders();

    public void report() throws IOException {
        applyOnFiles(this::reportFile);
    }

    public void parse() throws IOException {
        applyOnFiles(this::parseFile);
    }

    private void parseFile(Reader reader) {

        SqlParser sqlParser = SqlParser.create(reader, this.repository.getParserConfig());
        try {
            val stmts = sqlParser.parseStmtList();
            log.info("{} statements parsed", stmts.size());
        } catch (SqlParseException e) {
            throw new RuntimeException(e);
        }
    }

    private void applyOnFiles(Consumer<Reader> consumer) throws IOException {
        for (Iterator<Reader> it = this.getReaders(); it.hasNext(); ) {
            var reader = it.next();
            consumer.accept(reader);
            reader.close();
        }
    }

    @SneakyThrows
    private void reportFile(Reader reader) {
        SqlParser sqlParser = SqlParser.create(reader, this.repository.getParserConfig());
        val stmts = sqlParser.parseStmtList();
        for(val stmt : stmts) {
            Statements.apply(stmt, this.repository);
            SqlKind kind = stmt.getKind();
            log.warn("Failing back to default processor");
            if (kind == SqlKind.CREATE_VIEW) {
                addView(stmt);
                continue;
            }
            log.info("Skip statemt of kind {}", kind);
        }
    }

    public LineageRepository getLineageRepository() {
        return this.repository;
    }

    public record LineageReportItem(String name, RelNode plan, String sql, ArrayList<Set<LineageItems.TableAttribute>> lineage) {
    }


    private void addView(SqlNode stmt) {
        log.info("View statement");
    }


}
