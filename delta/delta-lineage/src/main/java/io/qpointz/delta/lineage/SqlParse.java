package io.qpointz.delta.lineage;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.sql.*;
import org.apache.calcite.sql.ddl.SqlColumnDeclaration;
import org.apache.calcite.sql.ddl.SqlCreateTable;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.parser.ddl.SqlDdlParserImpl;
import org.apache.calcite.sql.validate.SqlConformanceEnum;
import org.apache.calcite.tools.Frameworks;
import org.apache.calcite.tools.Planner;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

@Slf4j
public class SqlParse {

    @Getter
    private final Path startPath;

    private final Map<SqlIdentifier, LineageTable> tables = new HashMap<>();
    private final SqlDialect.DatabaseProduct databaseProduct;

    private CalciteConnection connection;

    private Planner planner;

    public SqlParse(Path startPath, SqlDialect.DatabaseProduct product) {
        this.startPath = startPath;
        this.databaseProduct = product;
        createSchema();
    }

    public void parse() throws IOException {
        Files.walk(this.getStartPath(), FileVisitOption.FOLLOW_LINKS)
                .filter(Files::isRegularFile)
                .forEach(this::parseFile);
    }

    @SneakyThrows
    private void parseFile(Path path) {
        val reader = Files.readAllLines(path).stream().collect(Collectors.joining(System.lineSeparator()));

        SqlParser sqlParser = SqlParser.create(reader, this.getParserConfig());
        val stmts = sqlParser.parseStmtList();
        for(val stmt : stmts) {
            SqlKind kind = stmt.getKind();
            if (kind == SqlKind.CREATE_TABLE ) {
                addTable(stmt);
                continue;
            }
            if (kind == SqlKind.CREATE_VIEW) {
                addView(stmt);
                continue;
            }

            if (kind == SqlKind.SELECT) {
                addSelect(stmt);
                continue;
            }

            log.info("Skip statemt of kind {}", kind);

        }
    }

    @SneakyThrows
    private void addSelect(SqlNode stmt) {
        log.info("Select statement");
        val sqlstring = stmt.toSqlString(this.databaseProduct.getDialect());
        val parsed = planner.parse(sqlstring.getSql());
        val validated = planner.validate(stmt);
        val rel = planner.rel(validated).rel;
        log.info(rel.explain());
    }

    @SneakyThrows
    private void createSchema() {
        Class.forName("org.apache.calcite.jdbc.Driver");
        this.connection = DriverManager
                .getConnection("jdbc:calcite:", new Properties())
                .unwrap(CalciteConnection.class);
        val schema = this.connection.getRootSchema();
        for(val vk : this.tables.entrySet()) {
            String tableName = vk.getKey().toString();
            schema.add(tableName, vk.getValue());
        }

        val config = Frameworks.newConfigBuilder()
                .defaultSchema(this.connection.getRootSchema())
                .parserConfig(this.getParserConfig())
                .build();

        this.planner = Frameworks.getPlanner(config);
    }

    private SqlParser.Config getParserConfig() {
        SqlParser.Config config = SqlParser.config();
        this.databaseProduct.getDialect().configureParser(config);
        return config
                .withParserFactory(SqlDdlParserImpl.FACTORY);
    }

    private void addView(SqlNode stmt) {
        log.info("View statement");
    }

    @SneakyThrows
    private void addTable(SqlNode stmt) {
        val ct = (SqlCreateTable)stmt;
        val columns = ct.columnList.stream()
                .map(k-> (SqlColumnDeclaration)k)
                .toList();

        val tbl = new LineageTable(ct.name, columns);
        tables.put(ct.name, tbl);
        log.info("Create table");
    }

}
