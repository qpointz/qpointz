package io.qpointz.rapids.calcite;

import io.qpointz.rapids.schema.Attribute;
import io.qpointz.rapids.schema.Catalog;
import io.qpointz.rapids.schema.DataSet;
import io.qpointz.rapids.schema.Namespace;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.schema.Table;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public interface SchemaPlusCalciteHandler extends CalciteHandler /*, GraphQLHandler*/ {

    @Override
    default Catalog getCatalog() {
        var schema = this.getRootSchema();
        var datasets = getDataSets(Namespace.rootNs, schema);
        return Catalog.builder().dataSets(datasets).build();
    }

    private Collection<DataSet> getDataSets(Namespace ns, SchemaPlus schema) {
        final var datasets = new ArrayList<DataSet>();
        for (var tableName : schema.getTableNames()) {
            datasets.add(getDataSet(ns, tableName, schema.getTable(tableName)));
        }

        for (var subSchemaName : schema.getSubSchemaNames()) {
            final var childNs = ns.createChild(subSchemaName);
            final var schemaDatasets = getDataSets(childNs, schema.getSubSchema(subSchemaName));
            datasets.addAll(schemaDatasets);
        }

        return datasets;
    }

    private DataSet getDataSet(Namespace ns, String name, Table table) {
        var attributes = this.getAttributes(table);
        return DataSet.builder()
                .name(name)
                .namespace(ns)
                .attributes(attributes)
                .build();

    }

    private List<Attribute> getAttributes(Table table) {
        var rowType = table.getRowType(this.getTypeFactory());
        var res = new ArrayList<Attribute>();
        for (var fld: rowType.getFieldList()) {
            final var attribute = Attribute.builder()
                    .index(fld.getIndex())
                    .name(fld.getName())
                    .type(fld.getType())
                    .build();
            res.add(attribute);
        }
        return res;
    }
}
