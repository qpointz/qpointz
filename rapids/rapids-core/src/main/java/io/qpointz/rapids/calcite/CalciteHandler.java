package io.qpointz.rapids.calcite;

import io.qpointz.rapids.schema.Catalog;
import org.apache.calcite.avatica.Meta;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.tools.FrameworkConfig;
import org.apache.calcite.tools.RelRunner;

import java.sql.SQLException;

public interface CalciteHandler {

    Meta getMeta();

    Catalog getCatalog();

    SchemaPlus getRootSchema();

    RelDataTypeFactory getTypeFactory();

    FrameworkConfig getFrameworkConfig();

    RelRunner getRelRunner() throws SQLException;
}
