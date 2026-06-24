package io.qpointz.mill.data.odata.type

import com.sdl.odata.api.edm.model.PrimitiveType
import io.qpointz.mill.proto.DataType
import io.qpointz.mill.proto.LogicalDataType

/**
 * Maps Mill physical [DataType] values to RWS OData [PrimitiveType] names.
 */
class MillTypeToEdmMapper {

    /**
     * @param dataType Mill proto column type
     * @return OData primitive type fully-qualified name (e.g. {@code Edm.String})
     */
    fun toEdmTypeName(dataType: DataType): String {
        val primitive = when (dataType.type.typeId) {
            LogicalDataType.LogicalDataTypeId.TINY_INT,
            LogicalDataType.LogicalDataTypeId.SMALL_INT -> PrimitiveType.INT16
            LogicalDataType.LogicalDataTypeId.INT -> PrimitiveType.INT32
            LogicalDataType.LogicalDataTypeId.BIG_INT -> PrimitiveType.INT64
            LogicalDataType.LogicalDataTypeId.BOOL -> PrimitiveType.BOOLEAN
            // Power BI incremental refresh sends DateTimeOffset range parameters; Edm.Date rejects them
            // client-side before the request reaches Mill.
            LogicalDataType.LogicalDataTypeId.DATE -> PrimitiveType.DATE_TIME_OFFSET
            LogicalDataType.LogicalDataTypeId.TIME -> PrimitiveType.TIME_OF_DAY
            LogicalDataType.LogicalDataTypeId.TIMESTAMP,
            LogicalDataType.LogicalDataTypeId.TIMESTAMP_TZ -> PrimitiveType.DATE_TIME_OFFSET
            LogicalDataType.LogicalDataTypeId.FLOAT -> PrimitiveType.SINGLE
            LogicalDataType.LogicalDataTypeId.DOUBLE -> PrimitiveType.DOUBLE
            LogicalDataType.LogicalDataTypeId.BINARY -> PrimitiveType.BINARY
            LogicalDataType.LogicalDataTypeId.UUID -> PrimitiveType.GUID
            LogicalDataType.LogicalDataTypeId.STRING,
            LogicalDataType.LogicalDataTypeId.NOT_SPECIFIED_TYPE,
            LogicalDataType.LogicalDataTypeId.INTERVAL_DAY,
            LogicalDataType.LogicalDataTypeId.INTERVAL_YEAR,
            LogicalDataType.LogicalDataTypeId.UNRECOGNIZED -> PrimitiveType.STRING
        }
        return primitive.fullyQualifiedName
    }

    /**
     * @param dataType Mill proto column type
     * @return whether the column allows null in OData EDM
     */
    fun isNullable(dataType: DataType): Boolean =
        dataType.nullability != DataType.Nullability.NOT_NULL
}
