package io.qpointz.delta.calcite;

import io.qpointz.delta.service.DeltaServiceBase;
import io.qpointz.delta.service.ExecutionProvider;
import io.qpointz.delta.service.SchemaProvider;
import io.qpointz.delta.service.SqlParserProvider;
import lombok.val;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.sql.parser.SqlParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.bind.Name;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

@Configuration
public class CalciteDeltaServiceCtx {

    @Bean
    @Qualifier("CALCITE_CONNECTION_PROPS")
    public static Properties calciteConnectionProperties(CalciteDataServiceConfiguration configuration) throws ClassNotFoundException, SQLException {
        val props = new Properties();
        props.putAll(configuration.getConnection());
        return props;
    }

    @Bean
    public static CalciteConnection calciteConnection(@Qualifier("CALCITE_CONNECTION_PROPS") Properties properties
                                                      ) throws ClassNotFoundException, SQLException {
        Class.forName("org.apache.calcite.jdbc.Driver");
        return DriverManager
                .getConnection("jdbc:calcite:", properties)
                .unwrap(CalciteConnection.class);
    }

    @Bean
    public SchemaProvider schemaProvider(CalciteConnection connection) {
        return new SchemaPlusSchemaProvider(connection.getRootSchema(), connection.getTypeFactory());
    }

    @Bean
    public ExecutionProvider executionProvider(CalciteConnection connection) {
        return new CalciteExecutionProvider(connection);
    }

    @Bean
    public static SqlParserProvider sqlParserProvider(CalciteConnection calciteConnection) {
        val connectionConfig = calciteConnection.config();
        val parserConfig = SqlParser.Config.DEFAULT
                .withConformance(connectionConfig.conformance())
                .withCaseSensitive(connectionConfig.caseSensitive())
                .withLex(connectionConfig.lex())
                .withQuoting(connectionConfig.quoting());
        return new SubstraitSqlParserProvider(parserConfig, calciteConnection);
    }
}
