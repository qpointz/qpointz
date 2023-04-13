package io.qpointz.rapids.calcite;

import io.qpointz.rapids.schema.Catalog;
import org.apache.calcite.avatica.Meta;
import org.apache.calcite.schema.SchemaPlus;

import java.sql.SQLException;

public interface CalciteHandler {

    Meta getMeta();

    Catalog getCatalog();

    SchemaPlus getRootSchema();

}
