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
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;

import java.util.Collection;
import java.util.List;

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


    public CsdlEntityType getEntityType(final FullQualifiedName entityTypeName) {
        assert  entityTypeName!=null;
        assert  entityTypeName.getNamespace().equals(this.namespace);

        final var tableName = entityTypeName.getName();
        final var properties = this.asSdlProperties(this.schema.getTable(tableName).getRowType(this.typeFactory));
        return new CsdlEntityType()
                //.setKey() //TODO:key is missing for entity types
                .setName(tableName)
                .setProperties(properties)
                //.setNavigationProperties() //TODO:set navigation properties
                ;
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
