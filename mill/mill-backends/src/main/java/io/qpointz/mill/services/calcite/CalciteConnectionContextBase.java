package io.qpointz.mill.services.calcite;

import lombok.val;
import org.apache.calcite.jdbc.CalciteSchema;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.tools.FrameworkConfig;
import org.apache.calcite.tools.Frameworks;
import org.apache.calcite.tools.RelRunner;

import java.sql.SQLException;

public abstract class CalciteConnectionContextBase implements CalciteContext {


    @Override
    public RelDataTypeFactory getTypeFactory() {
        return this.getCalciteConnection().getTypeFactory();
    }

    @Override
    public RelRunner getRelRunner() throws SQLException {
        return this.getCalciteConnection()
                .unwrap(RelRunner.class);
    }

    @Override
    public SchemaPlus getRootSchema() {
        return this.getCalciteConnection().getRootSchema()
                .unwrap(SchemaPlus.class);
    }

    @Override
    public CalciteSchema getCalciteRootSchema() {
        return this.getCalciteConnection().getRootSchema()
                .unwrap(CalciteSchema.class);
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
        return Frameworks.newConfigBuilder()
                .parserConfig(this.getParserConfig())
                .defaultSchema(this.getRootSchema())
                .build();
    }


}
