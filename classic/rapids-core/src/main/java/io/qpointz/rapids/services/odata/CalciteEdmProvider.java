package io.qpointz.rapids.services.odata;

import io.qpointz.rapids.calcite.CalciteHandler;
import jdk.jshell.spi.ExecutionControl;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.apache.avro.generic.GenericData;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.*;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

@AllArgsConstructor
@Builder
public class CalciteEdmProvider extends CsdlAbstractEdmProvider {


    private final String namespace;

    private final SchemaPlus calciteSchema;

    private final CalciteHandler calciteHandler;



    String containerName() {
        return this.calciteSchema.getName();
    }
    
    @Getter(lazy = true)
    private final CalciteEdmAdapter adapter = createAdapter();

    private CalciteEdmAdapter createAdapter() {
        return new CalciteEdmAdapter(this.calciteSchema, this.namespace, this.containerName(), this.calciteHandler.getTypeFactory());
    }


    @Override
    public CsdlEntityType getEntityType(final FullQualifiedName entityTypeName) throws ODataException {
        return this.getAdapter().getEntityType(entityTypeName);
    }

    @Override
    public CsdlEntitySet getEntitySet(final FullQualifiedName entityContainer, final String entitySetName) throws ODataException {
        final var maybeSet =  this.getAdapter().getEntitySet(entitySetName);
        if (maybeSet.isEmpty()) {
            throw new ODataApplicationException("%s entity set not found".formatted(entitySetName),
                                                HttpStatusCode.NOT_FOUND.getStatusCode(),
                                                Locale.ENGLISH);
        }

        return maybeSet.get();
    }

    @Override
    public List<CsdlSchema> getSchemas() throws ODataException {
        final var types = this.getAdapter().entityTypes().toList();
        final var schema = new CsdlSchema()
                .setNamespace(this.namespace)
                .setEntityTypes(types)
                .setEntityContainer(this.getEntityContainer());
        return List.of(schema);
    }

    @Override
    public CsdlEntityContainerInfo getEntityContainerInfo(final FullQualifiedName entityContainerName)
            throws ODataException {
        return new CsdlEntityContainerInfo()
                .setContainerName(this.getAdapter().containerFqdn());
    }

    @Override
    public CsdlEntityContainer getEntityContainer() {
        final var container = new CsdlEntityContainer();
        container.setName(this.containerName());
        container.setEntitySets(this.getAdapter().entitySets().toList());
        return container;
    }
}
