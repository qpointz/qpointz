package io.qpointz.rapids.calcite;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.calcite.avatica.Meta;
import org.apache.calcite.avatica.jdbc.JdbcMeta;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.plan.Contexts;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.tools.FrameworkConfig;
import org.apache.calcite.tools.Frameworks;
import org.apache.calcite.tools.RelRunner;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

@Slf4j
public class StandardCalciteHanlder implements SchemaPlusCalciteHandler  {

    private final Properties properties;

    private static final String jdbcUrl = "jdbc:calcite:";

    @Getter(lazy = true)
    private final CalciteConnection calciteConnection = createCalciteConnection();

    private CalciteConnection createCalciteConnection() {
        //ugly hack :)
        try {
            var connection = DriverManager.getConnection(jdbcUrl, this.properties);
            return connection.unwrap(CalciteConnection.class);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public StandardCalciteHanlder(Properties defaultProperties) throws ClassNotFoundException, SQLException {
        log.debug("Resolve calcite JDBC classes");
        Class.forName("org.apache.calcite.jdbc.Driver");
        Class.forName("io.qpointz.rapids.formats.parquet.RapidsParquetSchemaFactory");
        this.properties = new Properties();
        this.properties.putAll(defaultProperties);
    }

    @Override
    public Meta getMeta() {
        try {
            return new JdbcMeta(StandardCalciteHanlder.jdbcUrl, this.properties);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public SchemaPlus getRootSchema() {
        return this.getCalciteConnection().getRootSchema();
    }

    @Override
    public RelDataTypeFactory getTypeFactory() {
        return this.getCalciteConnection().getTypeFactory();
    }

    @Override
    public FrameworkConfig getFrameworkConfig() {
        return Frameworks.newConfigBuilder()
                .defaultSchema(this.getRootSchema())
                .context(Contexts.of(this.getCalciteConnection().config()))
                .build();
    }

    @Override
    public RelRunner getRelRunner() throws SQLException {
        return this.getCalciteConnection().unwrap(RelRunner.class);
    }
}
