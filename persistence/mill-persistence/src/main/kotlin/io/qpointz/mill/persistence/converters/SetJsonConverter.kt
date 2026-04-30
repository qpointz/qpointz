package io.qpointz.mill.persistence.converters

import tools.jackson.core.type.TypeReference
import io.qpointz.mill.utils.JsonUtils
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

private val mapper = JsonUtils.defaultJsonMapper()

/**
 * JPA [AttributeConverter] that serializes [Set]<[String]> to/from a JSON text column.
 *
 * Shared persistence primitive — reusable by any domain persistence module.
 */
@Converter
class SetJsonConverter : AttributeConverter<Set<String>, String> {
    override fun convertToDatabaseColumn(attribute: Set<String>?): String =
        if (attribute == null) "[]" else mapper.writeValueAsString(attribute)

    override fun convertToEntityAttribute(dbData: String?): Set<String> =
        if (dbData.isNullOrBlank()) emptySet()
        else mapper.readValue(dbData, object : TypeReference<Set<String>>() {})
}
