package io.qpointz.delta.lineage.model;

import io.qpointz.delta.lineage.LineageTable;
import io.qpointz.delta.lineage.SqlParse;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.sql.SqlDialect;
import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.parser.ddl.SqlDdlParserImpl;
import org.apache.calcite.tools.Frameworks;
import org.apache.calcite.tools.Planner;

import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Slf4j
public class LineageRepository {
    private final SqlDialect.DatabaseProduct databaseProduct;

    private final Map<SqlIdentifier, LineageTable> tables = new HashMap<>();

    @Getter
    private final ArrayList<SqlParse.LineageReportItem> report = new ArrayList<>();

    private CalciteConnection connection;

    @Getter
    private Planner planner;

    public LineageRepository(SqlDialect.DatabaseProduct product) {
        this.databaseProduct = product;
        reset();
    }

    public SchemaPlus getRootSchema() {
        return this.connection.getRootSchema();
    }

    public RelDataTypeFactory getTypeFactory() {
        return this.connection.getTypeFactory();
    }

    public SqlParser.Config getParserConfig() {
        SqlParser.Config config = SqlParser.config();
        this.databaseProduct.getDialect().configureParser(config);
        return config
                .withParserFactory(SqlDdlParserImpl.FACTORY);
    }

    @SneakyThrows
    private void reset() {
        this.tables.clear();
        this.report.clear();
        Class.forName("org.apache.calcite.jdbc.Driver");
        Class.forName("org.apache.calcite.jdbc.CalciteJdbc41Factory");
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

    public SqlDialect getDialect() {
            return this.databaseProduct.getDialect();
    }

    public void addTable(LineageTable tbl) {
        this.connection.getRootSchema().add(tbl.getTableName().toString(), tbl);
        log.info("Create table");
    }
}
