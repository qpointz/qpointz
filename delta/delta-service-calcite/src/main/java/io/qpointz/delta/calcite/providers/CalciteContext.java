package io.qpointz.delta.calcite.providers;

import com.fasterxml.jackson.databind.type.TypeFactory;
import io.substrait.extension.ExtensionCollector;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.val;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.jdbc.CalciteSchema;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.tools.FrameworkConfig;
import org.apache.calcite.tools.Frameworks;
import org.apache.calcite.tools.RelRunner;

import java.sql.SQLException;

@AllArgsConstructor
public class CalciteContext {

    @Getter
    private final CalciteConnection calciteConnection;


    public RelDataTypeFactory getTypeFactory() {
        return this.getCalciteConnection().getTypeFactory();
    }

    public RelRunner getRelRunner() throws SQLException {
        return this.getCalciteConnection()
                .unwrap(RelRunner.class);
    }

    public SchemaPlus getRootSchema() {
        return calciteConnection.getRootSchema()
                .unwrap(SchemaPlus.class);
    }

    public CalciteSchema getCalciteRootSchema() {
        return calciteConnection.getRootSchema()
                .unwrap(CalciteSchema.class);
    }

    public SqlParser.Config getParserConfig() {
        val connectionConfig = this.getCalciteConnection().config();
        return SqlParser.Config.DEFAULT
                .withConformance(connectionConfig.conformance())
                .withCaseSensitive(connectionConfig.caseSensitive())
                .withLex(connectionConfig.lex())
                .withQuoting(connectionConfig.quoting());
    }

    public FrameworkConfig getFrameworkConfig() {
        return Frameworks.newConfigBuilder()
                .parserConfig(this.getParserConfig())
                .defaultSchema(this.getRootSchema())
                .build();
    }


}
