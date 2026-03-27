# General consideration 

All metadata imported in in global scope 

Below i will describe mapping of existing metadata into faceted one 

validate rules against standard facets defines in metadata/mill-metadata-core/src/main/resources/metadata/platform-facet-types.json

# descriptive 

descriptive.displayName =>  urn:mill/metadata/facet-type:descriptive.displayName
descriptive.description =>  urn:mill/metadata/facet-type:descriptive.description

# structural 

## when entity.type == TABLE 

urn:mill/metadata/facet-type:source-table.sourceType = "FLOW"
urn:mill/metadata/facet-type:source-table.package = ""
urn:mill/metadata/facet-type:source-table.name = structural.physicalName

## when entity.type == ATTRIBUTE

urn:mill/metadata/facet-type:source-column.name = structural.physicalName
urn:mill/metadata/facet-type:source-column.type = structural.physicalType
urn:mill/metadata/facet-type:source-column.nullable = structural.nullable
urn:mill/metadata/facet-type:source-column.isFK = structural.isForeignKey
urn:mill/metadata/facet-type:source-column.isPK = structural.isPrimaryKey

# relations

For each relation create a new facet with type urn:mill/metadata/facet-type:relation

urn:mill/metadata/facet-type:relation.name = relations[item].name
urn:mill/metadata/facet-type:relation.description = relations[item].description
urn:mill/metadata/facet-type:relation.cardinality = relations[item] (ENUM ONE_TO_MANY,ONE_TO_ONE,MANY_TO_MANY)
urn:mill/metadata/facet-type:relation.source.schema = relations[item].sourceTable.schema
urn:mill/metadata/facet-type:relation.source.table = relations[item].sourceTable.table
urn:mill/metadata/facet-type:relation.source.columns = relations[item].sourceAttributes
urn:mill/metadata/facet-type:relation.target.schema = relations[item].targetTable.schema
urn:mill/metadata/facet-type:relation.target.table = relations[item].targetTable.table
urn:mill/metadata/facet-type:relation.target.columns = relations[item].targetAttributes
urn:mill/metadata/facet-type:relation.expression = relations[item].joinSql


## overall logic and considerations
Existing global metadata facet types to be removes in code and scripts : 
- urn:mill/metadata/facet-type:structural   
- urn:mill/metadata/facet-type:concept
- urn:mill/metadata/facet-type:value-mapping