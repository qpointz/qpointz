package io.qpointz.mill.services.jdbc.providers;

import io.qpointz.mill.services.calcite.CalciteConnectionContextBase;
import io.qpointz.mill.services.calcite.CalciteContext;
import io.qpointz.mill.services.calcite.CalciteContextFactory;
import io.qpointz.mill.services.jdbc.configuration.JdbcCalciteConfiguration;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.calcite.adapter.jdbc.JdbcSchema;
import org.apache.calcite.jdbc.CalciteConnection;

import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Optional;
import java.util.Properties;

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
        val operand = new HashMap<String,Object>();
        operand.put("jdbcUrl", op.getUrl());
        operand.put("jdbcDriver", op.getDriver());

        op.getUser()
                .ifPresent(s -> operand.put("jdbcUser", s));

        op.getPassword()
                .ifPresent(s -> operand.put("jdbcPassword", s));

        op.getSchema()
                .ifPresent(s -> operand.put("jdbcSchema", s));

        op.getCatalog()
                .ifPresent(s -> operand.put("jdbcCatalog", s));

        val jdbcSchema = JdbcSchema.create(rootSchema, this.targetSchema.orElse("jdbc"), operand);

        rootSchema.add(this.targetSchema.orElse("jdbc"), jdbcSchema);

        return new JdbcCalciteContext(calciteConnection);
    }

}
