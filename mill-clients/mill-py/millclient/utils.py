import pyarrow as pa
from pyarrow import RecordBatch

from millclient.exceptions import MillError
from millclient.proto.io.qpointz.mill import ExecQueryResponse, Vector, Field


def __get_reader(field: Field):
    def __string_reader(vector: Vector, idx: int) -> str:
        return vector.string_vector.values[idx]

    def __bool_reader(vector: Vector, idx: int) -> bool:
        return vector.bool_vector.values[idx]

    if field.type.is_set("string") or field.type.is_set("varchar") or field.type.is_set("fixed_char"):
        return __string_reader
    if field.type.is_set("bool"):
        return __bool_reader

    raise MillError(f"Unknown field type: {field.type}")


def response_to_record_batch(response: ExecQueryResponse) -> RecordBatch:
    arrays = []
    fields = []
    cnt = response.vector.vector_size
    for field in response.vector.schema.fields:
        fields.append(pa.field(field.name, pa.string()))
        reader = __get_reader(field)
        vector = response.vector.vectors[field.field_idx]
        ca = []
        for idx in range(cnt):
            if vector.nulls.nulls[idx]:
                ca.append(None)
                continue
            ca.append(reader(vector, idx))
        arrays.append(ca)
    schema = pa.schema(fields)
    return RecordBatch.from_arrays(arrays, schema=schema)
