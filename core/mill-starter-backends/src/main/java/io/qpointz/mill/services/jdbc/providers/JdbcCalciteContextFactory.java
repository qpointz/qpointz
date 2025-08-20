package io.qpointz.mill.services.jdbc.providers;

import io.qpointz.mill.services.calcite.CalciteConnectionContextBase;
import io.qpointz.mill.services.calcite.CalciteContext;
import io.qpointz.mill.services.calcite.CalciteContextFactory;
import io.qpointz.mill.services.jdbc.configuration.JdbcCalciteConfiguration;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.calcite.adapter.jdbc.JdbcSchema;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.plan.Contexts;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.tools.FrameworkConfig;
import org.apache.calcite.tools.Frameworks;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

@AllArgsConstructor
@Slf4j
public class JdbcCalciteContextFactory implements CalciteContextFactory {

    @Getter
    @Setter
    private Properties connectionProperty;

    @Getter
    @Setter
    private JdbcCalciteConfiguration jdbcConnection;

    @Getter
    @Setter
    public Optional<String> targetSchema ;

    @AllArgsConstructor
    private static class JdbcCalciteContext extends CalciteConnectionContextBase {

        @Getter
        private final CalciteConnection connection;

        @Override
        public CalciteConnection getCalciteConnection() {
            return connection;
        }

        @Override
        public void close() throws Exception {
            connection.close();
        }

        @Override
        public SqlParser.Config getParserConfig() {
            val connectionConfig = this.getCalciteConnection().config();
            return SqlParser.Config.DEFAULT
                    .withConformance(connectionConfig.conformance())
                    .withCaseSensitive(connectionConfig.caseSensitive())
                    .withLex(connectionConfig.lex())
                    .withQuoting(connectionConfig.quoting());
        }

        @Override
        public FrameworkConfig getFrameworkConfig() {
            val connectionConfig = this.getCalciteConnection()
                    .config();

            return Frameworks.newConfigBuilder()
                    .context(Contexts.of(connectionConfig))
                    .parserConfig(this.getParserConfig())
                    .defaultSchema(this.getRootSchema())
                    .build();
        }
    }

    @Override
    public CalciteContext createContext() throws Exception {
        Class.forName("org.apache.calcite.jdbc.Driver");

        val calciteConnection = DriverManager
                .getConnection("jdbc:calcite:", this.getConnectionProperty())
                .unwrap(CalciteConnection.class);

        log.trace("Getting root schema");
        val rootSchema = calciteConnection.getRootSchema();

        log.trace("Getting driver");
        val op = this.getJdbcConnection();
        log.trace("Connection is null {}", (op != null));

        Class.forName(op.getDriver());
        
        if (!op.getMultiSchema()) {
            //add single schema
            addSingleSchema(rootSchema);
        } else {
            //add multiple schemas
            log.trace("Adding multiple schemas");
            addMultipleSchemas(rootSchema);
        }
        
        return new JdbcCalciteContext(calciteConnection);
    }

    private void addMultipleSchemas(SchemaPlus rootSchema) throws SQLException {
        val op = this.getJdbcConnection();
        val operand = new Properties();
        op.getUser()
                .ifPresent(s -> operand.put("jdbcUser", s));
        op.getPassword()
                .ifPresent(s -> operand.put("jdbcPassword", s));
        try (val con = DriverManager.getConnection(op.getUrl(), operand)) {
            val meta = con.getMetaData();
            try (val rs = meta.getSchemas()) {
                var schemaIdx = 0;
                while (rs.next()) {
                    val catalog = Optional.ofNullable(rs.getString("TABLE_CATALOG"));
                    val schema = Optional.ofNullable(rs.getString("TABLE_SCHEM"));
                    createJdbcSchema(rootSchema, schema.orElse(String.format("schema_%s", ++schemaIdx)), catalog, schema);
                }
            }
        }
    }

    private void addSingleSchema(SchemaPlus rootSchema) {
        val op = this.getJdbcConnection();
        createJdbcSchema(rootSchema, this.targetSchema.orElse("jdbc"), op.getCatalog(), op.getSchema());
    }

    private void createJdbcSchema(SchemaPlus rootSchema, String targetName, Optional<String> catalog, Optional<String> schema) {
        val op = this.getJdbcConnection();

        val operand = new HashMap<String,Object>();
        operand.put("jdbcUrl", op.getUrl());
        operand.put("jdbcDriver", op.getDriver());

        op.getUser()
                .ifPresent(s -> operand.put("jdbcUser", s));

        op.getPassword()
                .ifPresent(s -> operand.put("jdbcPassword", s));

        catalog
                .ifPresent(s -> operand.put("jdbcCatalog", s));

        schema
                .ifPresent(s -> operand.put("jdbcSchema", s));

        val jdbcSchema = JdbcSchema.create(rootSchema, targetName, operand);
        rootSchema.add(targetName, jdbcSchema);
    }



}
