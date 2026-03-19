package io.qpointz.mill.persistence.converters

import com.fasterxml.jackson.core.type.TypeReference
import io.qpointz.mill.utils.JsonUtils
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

private val mapper = JsonUtils.defaultJsonMapper()

/**
 * JPA [AttributeConverter] that serializes [Map]<[String], [Any]?> to/from a JSON text column.
 *
 * Shared persistence primitive — reusable by any domain persistence module.
 */
@Converter
class MapJsonConverter : AttributeConverter<Map<String, Any?>, String> {
    override fun convertToDatabaseColumn(attribute: Map<String, Any?>?): String =
        if (attribute == null) "{}" else mapper.writeValueAsString(attribute)

    override fun convertToEntityAttribute(dbData: String?): Map<String, Any?> =
        if (dbData.isNullOrBlank()) emptyMap()
        else mapper.readValue(dbData, object : TypeReference<Map<String, Any?>>() {})
}
