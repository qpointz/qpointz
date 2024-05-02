package io.qpointz.delta.lineage;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.sql.*;
import org.apache.calcite.sql.ddl.SqlColumnDeclaration;
import org.apache.calcite.sql.ddl.SqlCreateTable;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.parser.ddl.SqlDdlParserImpl;
import org.apache.calcite.tools.Frameworks;
import org.apache.calcite.tools.Planner;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class SqlParse {

    @Getter
    private final List<File> files;

    private final Map<SqlIdentifier, LineageTable> tables = new HashMap<>();
    private final ArrayList<LineageReportItem> report = new ArrayList<>();

    private final SqlDialect.DatabaseProduct databaseProduct;
    private CalciteConnection connection;
    private Planner planner;


    public SqlParse(List<File> files, SqlDialect.DatabaseProduct product) {
        this.files = files;
        this.databaseProduct = product;
        createSchema();
    }

    public static SqlParse create(File[] files, SqlDialect.DatabaseProduct databaseProduct) throws IOException {
        val list = new ArrayList<File>();
        for (File file : files) {
            list.addAll(toFiles(file));
        }
        return new SqlParse(list, databaseProduct);
    }

    private static List<File> toFiles(File file) throws IOException {
        if (file.isFile()) {
            return List.of(file);
        }

        return Files.walk(file.toPath(), FileVisitOption.FOLLOW_LINKS)
                .filter(Files::isRegularFile)
                .map(Path::toFile)
                .collect(Collectors.toList());
    }

    public void parse() throws IOException {
        for (var f: this.files) {
            parseFile(f);
        }
    }

    @SneakyThrows
    private void parseFile(File path) {
        val reader = Files.readAllLines(path.toPath())
                .stream()
                .collect(Collectors.joining(System.lineSeparator()));

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

    public record LineageReportItem(String name, RelNode plan, String sql, ArrayList<Set<LineageItems.TableAttribute>> lineage) {
    }

    @SneakyThrows
    private void addSelect(SqlNode stmt) {
        log.info("Select statement");
        val sqlstring = stmt.toSqlString(this.databaseProduct.getDialect());
        val parsed = planner.parse(sqlstring.getSql());
        val validated = planner.validate(parsed);
        val rel = planner.rel(validated).rel;
        val shuttle = new LineageShuttle();
        rel.accept(shuttle);
        val lin = shuttle.attributesOf(rel);
        report.add(new LineageReportItem("QUERY", rel, sqlstring.getSql(), lin));
    }

    @SneakyThrows
    private void createSchema() {
        this.tables.clear();
        this.report.clear();
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
        this.connection.getRootSchema().add(ct.name.toString(), tbl);
        log.info("Create table");
    }

    public StringBuilder report() {
        val sb = new StringBuilder();
        for (val i : this.report) {
            report(i, sb);
        }
        return sb;
    }

    private void report(LineageReportItem i, StringBuilder sb) {
        sb.append(String.format("OBJECT:%s\n", i.name));
        sb.append(String.format("SQL:\n%s\n", i.sql));
        sb.append(String.format("PLAN:\n%s", i.plan.explain()));
        val rowType = i.plan.getRowType().getFieldList();
        sb.append("LINEAGE:\n");
        for (int idx=0;idx< rowType.size();idx++) {
            val name = rowType.get(idx).getName();
            val type = rowType.get(idx).getType();
            val attibs = i.lineage.get(idx);
            sb.append(String.format("\t%s (%s):\n", name, type.getSqlTypeName().toString()));
            for (val at: attibs) {
                sb.append(String.format("\t\t<-%s.%s\n", at.table().get(0), at.attribute()));
            }
        }
        sb.append("\n\n");
    }


}
