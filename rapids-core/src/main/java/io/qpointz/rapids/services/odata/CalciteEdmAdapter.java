package io.qpointz.rapids.services.odata;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.rel.type.RelDataTypeField;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.olingo.commons.api.edm.EdmPrimitiveType;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@AllArgsConstructor
@Builder
public class CalciteEdmAdapter {

    @Getter
    private final SchemaPlus schema;

    @Getter
    private final String namespace;

    @Getter
    private final String containerName;

    @Getter
    private final RelDataTypeFactory typeFactory;

    public FullQualifiedName containerFqdn() {
        return new FullQualifiedName(this.namespace, this.containerName);
    }

    private Stream<TableEntityInfo> getTableInfo () {
        return this.schema
                .getTableNames()
                .stream()
                .map(p-> TableEntityInfo.create(this.schema.getName(), p, this.namespace, this.containerName));
    }

    public Stream<CsdlEntityType> entityTypes() {
        return this.getTableInfo().map(this::getEntityType);
    }

    private CsdlEntityType getEntityType(TableEntityInfo tableInfo) {
        final var rowType = this.schema
                .getTable(tableInfo.getTableName())
                .getRowType(this.typeFactory);

        final var properties = this.asSdlProperties(rowType);

        return new CsdlEntityType()
                .setName(tableInfo.getEntityTypeName())
                .setProperties(properties);
    }

    public CsdlEntityType getEntityType(final FullQualifiedName entityTypeName) {
        final var maybeTybleInfo = this.getTableInfo()
                .filter(p-> p.getEntityTypeFqdn().equals(entityTypeName))
                .findFirst();

        if (maybeTybleInfo.isPresent()) {
            return getEntityType(maybeTybleInfo.get());
        }

        throw new RuntimeException("%s type not found".formatted(entityTypeName.getFullQualifiedNameAsString()));
    }

    private FullQualifiedName tableEntityTypeFqdn(String tableName) {
        return new FullQualifiedName(this.namespace, tableName);
    }

    public Stream<CsdlEntitySet> entitySets() {
        return this.getTableInfo()
                .map(p-> new CsdlEntitySet()
                        .setName(p.getTableName())
                        .setType(p.getEntityTypeFqdn()));
    }

    public Optional<CsdlEntitySet> getEntitySet(String name) {
        return this.entitySets()
                .filter(p-> p.getName().equals(name))
                .findFirst();
    }

    private List<CsdlProperty> asSdlProperties(RelDataType rowType) {
        return rowType.getFieldList().stream()
                .map(this::fieldToProperty)
                .toList();
    }

    private CsdlProperty fieldToProperty(RelDataTypeField relField) {
        final var relType = relField.getType();
        final var typeConverter = new CalciteOdataTypeConverter(this.typeFactory);
        return new CsdlProperty()
                .setName(relField.getName())
                .setNullable(relType.isNullable())
                .setType(typeConverter.fromCalcite(relType));
    }

}
