package io.qpointz.mill.data.odata.type

import com.sdl.odata.api.edm.model.PrimitiveType
import io.qpointz.mill.proto.DataType
import io.qpointz.mill.proto.LogicalDataType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class MillTypeToEdmMapperTest {

    private val mapper = MillTypeToEdmMapper()

    @Test
    fun shouldMapDateToDateTimeOffsetForBiCompatibility() {
        val edmType = mapper.toEdmTypeName(
            DataType.newBuilder()
                .setType(
                    LogicalDataType.newBuilder()
                        .setTypeId(LogicalDataType.LogicalDataTypeId.DATE),
                )
                .build(),
        )

        assertThat(edmType).isEqualTo(PrimitiveType.DATE_TIME_OFFSET.fullyQualifiedName)
    }
}
