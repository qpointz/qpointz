package io.qpointz.mill.data.odata.edm

import com.sdl.odata.api.edm.model.EntityDataModel
import com.sdl.odata.api.edm.model.EntitySet
import com.sdl.odata.api.edm.model.EntityType
import com.sdl.odata.edm.model.EntityContainerImpl
import com.sdl.odata.edm.model.EntityDataModelImpl
import com.sdl.odata.edm.model.EntitySetImpl
import com.sdl.odata.edm.model.EntityTypeImpl
import com.sdl.odata.edm.model.KeyImpl
import com.sdl.odata.edm.model.PropertyImpl
import com.sdl.odata.edm.model.PropertyRefImpl
import com.sdl.odata.edm.model.SchemaImpl
import io.qpointz.mill.data.odata.type.MillTypeToEdmMapper
import io.qpointz.mill.data.schema.SchemaFacetService
import io.qpointz.mill.data.schema.SchemaTableWithFacets
import io.qpointz.mill.metadata.service.MetadataContext

/**
 * Builds an RWS [EntityDataModel] from merged physical schema and metadata facets.
 */
class EntityDataModelFactory @JvmOverloads constructor(
    private val schemaFacetService: SchemaFacetService,
    private val typeMapper: MillTypeToEdmMapper = MillTypeToEdmMapper(),
    private val navigationPropertyBuilder: NavigationPropertyBuilder = NavigationPropertyBuilder(),
) {

    /**
     * @param schemaName physical schema to expose
     * @param context metadata scope for facet resolution
     * @return OData EDM for tables in the given schema only
     */
    fun buildForSchema(
        schemaName: String,
        context: MetadataContext = MetadataContext.global(),
    ): EntityDataModel {
        val facetResult = schemaFacetService.getSchemas(context)
        val schema = facetResult.schemas.singleOrNull { it.schemaName == schemaName }
            ?: return emptyModel(schemaName)

        val entitySets = mutableListOf<EntitySet>()
        val namespace = EntitySetNaming.entityTypeNamespace(schemaName)
        val schemaBuilder = SchemaImpl.Builder().setNamespace(namespace)

        schema.tables.forEach { table ->
            val entityType = buildEntityType(table)
            schemaBuilder.addType(entityType)
            entitySets += EntitySetImpl.Builder()
                .setName(table.tableName)
                .setTypeName(entityType.fullyQualifiedName)
                .setIsIncludedInServiceDocument(true)
                .build()
        }

        val container = EntityContainerImpl.Builder()
            .setName(schemaName)
            .setNamespace(EntitySetNaming.MODEL_NAMESPACE_PREFIX)
            .addEntitySets(entitySets)
            .build()

        return EntityDataModelImpl(container, listOf(schemaBuilder.build()))
    }

    private fun emptyModel(schemaName: String): EntityDataModel {
        val container = EntityContainerImpl.Builder()
            .setName(schemaName)
            .setNamespace(EntitySetNaming.MODEL_NAMESPACE_PREFIX)
            .build()
        return EntityDataModelImpl(container, emptyList())
    }

    private fun buildEntityType(table: SchemaTableWithFacets): EntityType {
        val namespace = EntitySetNaming.entityTypeNamespace(table.schemaName)
        val builder = EntityTypeImpl.Builder()
            .setNamespace(namespace)
            .setName(table.tableName)
            .setIsAbstract(false)

        table.columns.forEach { column ->
            val property = PropertyImpl.Builder()
                .setName(column.columnName)
                .setTypeName(typeMapper.toEdmTypeName(column.dataType))
                .setIsNullable(typeMapper.isNullable(column.dataType))
                .build()
            builder.addStructuralProperty(property)
        }

        navigationPropertyBuilder.buildForTable(table).forEach { nav ->
            builder.addStructuralProperty(nav)
        }

        val keyColumn = table.columns.firstOrNull()?.columnName
        if (keyColumn != null) {
            builder.setKey(KeyImpl(listOf(PropertyRefImpl(keyColumn))))
        }

        return builder.build()
    }
}
