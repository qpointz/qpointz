package io.qpointz.rapids.services.odata;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlAbstractEdmProvider;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer;

import java.util.Collection;

@AllArgsConstructor
@Builder
public class CalciteEdmProvider extends CsdlAbstractEdmProvider {

    private final String namespace;

    private final SchemaPlus calciteSchema;
    String containerName() {
        return this.calciteSchema.getName();
    }
    
    @Getter(lazy = true)
    private final Collection<TableEntityInfo> tableEntities = loadTableEntities();

    private Collection<TableEntityInfo> loadTableEntities() {
        assert this.calciteSchema != null;
        final var schemaName = this.calciteSchema.getName();
        final var containerFqdn = new FullQualifiedName(this.namespace, containerName());
        return calciteSchema.getTableNames().stream().map(p ->
            TableEntityInfo.create(schemaName, p, containerFqdn)
        ).toList();
    }

    @Override
    public CsdlEntityContainer getEntityContainer() {
        final var container = new CsdlEntityContainer();
        container.setName(this.containerName());
        return container;
    }
}
