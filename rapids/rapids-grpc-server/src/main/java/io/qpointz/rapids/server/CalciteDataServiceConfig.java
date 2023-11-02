package io.qpointz.rapids.server;


import lombok.*;
import org.apache.calcite.avatica.util.Quoting;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.jdbc.JavaTypeFactoryImpl;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.schema.Schema;
import org.apache.calcite.schema.SchemaFactory;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.validate.SqlConformanceEnum;
import org.apache.calcite.tools.Frameworks;
import org.apache.calcite.tools.RelRunner;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;

@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CalciteDataServiceConfig {
//
//    public SchemaPlus getRootSchema() {
//        return this.connection.getRootSchema();
//    }
//
//    public RelDataTypeFactory getTypeFactory() {
//        return this.connection.getTypeFactory();
//    }
//
//    public RelRunner getRelRunner() throws SQLException {
//        return this.connection.unwrap(RelRunner.class);
//    }

    @Getter
    private CalciteConnection connection;

    @Getter SqlParser.Config parserConfig;

    public static class CalciteDataServiceConfigBuilder {

        @SneakyThrows
        public CalciteDataServiceConfigBuilder defaultConfig() {
            Class.forName("org.apache.calcite.jdbc.Driver");
            CalciteConnection conn = DriverManager
                    .getConnection("jdbc:calcite:")
                    .unwrap(CalciteConnection.class);
            return this.defaultConfig(conn);
        }

        public CalciteDataServiceConfigBuilder defaultConfig(CalciteConnection conn)  {
            this.connection(conn);
            final var parserConfig = SqlParser.Config.DEFAULT
                    .withQuoting(Quoting.BACK_TICK)
                    .withCaseSensitive(true)
                    .withConformance(SqlConformanceEnum.DEFAULT);
            return this
                    .parserConfig(parserConfig);
        }

        public CalciteDataService buildService() {
            return CalciteDataService
                    .create(this.build());
        }

        public SchemaPlus rootSchema() {
            return this.connection.getRootSchema();
        }

        public CalciteDataServiceConfigBuilder add(SchemaFactory schemaFactory, String name, Map<String, Object> operand) {
            final var schema = schemaFactory.create(this.rootSchema(), name, operand);
            if (!this.rootSchema().getSubSchemaNames().contains(name)) {
                this.rootSchema().add(name, schema);
            }
            return this;
        }

        public CalciteDataServiceConfigBuilder add(Schema schema, String name) {
            this.rootSchema().add(name, schema);
            return this;
        }

        private static SqlParser.Config parserConfigFromConnection(CalciteConnection connection) {
            final var config = connection.config();
            return SqlParser.Config.DEFAULT
                    .withQuoting(config.quoting())
                    .withCaseSensitive(config.caseSensitive())
                    .withLex(config.lex())
                    .withConformance(config.conformance())
                    .withQuotedCasing(config.quotedCasing());
        }





    }


}
