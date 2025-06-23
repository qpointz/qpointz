package io.qpointz.mill.services.metadata;

import io.qpointz.mill.services.metadata.model.Model;
import io.qpointz.mill.services.metadata.model.Relation;
import io.qpointz.mill.services.metadata.model.Schema;
import io.qpointz.mill.services.metadata.model.Table;

import java.util.Collection;
import java.util.Optional;

public interface MetadataProvider {

    Model getModel();
    Collection<Schema> getSchemas();
    Collection<Table> getTables(String schemaName);
    Collection<Relation> getRelations();
    Optional<Table> getTable(String schemaName, String tableName);

}
