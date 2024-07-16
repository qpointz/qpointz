package io.qpointz.mill.service.calcite;

import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.jdbc.CalciteSchema;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.tools.FrameworkConfig;
import org.apache.calcite.tools.RelRunner;

import java.sql.SQLException;

public interface CalciteContext extends AutoCloseable {

    CalciteConnection getCalciteConnection();

    RelDataTypeFactory getTypeFactory();

    RelRunner getRelRunner() throws SQLException;

    SchemaPlus getRootSchema();

    CalciteSchema getCalciteRootSchema();

    SqlParser.Config getParserConfig();

    FrameworkConfig getFrameworkConfig();

}
