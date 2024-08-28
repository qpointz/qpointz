import uuid
from datetime import timedelta, datetime, date

from pandas import DataFrame
from pyarrow import RecordBatch
import pyarrow as pa

from millclient.exceptions import MillError
from millclient.proto.io.qpointz.mill import ExecQueryResponse, Vector, Field, LogicalDataTypeLogicalDataTypeId, \
    VectorBlock


def __get_reader(type_id: LogicalDataTypeLogicalDataTypeId):
    def __string_reader(vector: Vector, idx: int) -> str:
        return vector.string_vector.values[idx]

    def __bool_reader(vector: Vector, idx: int) -> bool:
        return vector.bool_vector.values[idx]

    def __int_reader(vector: Vector, idx: int) -> int:
        return vector.i32_vector.values[idx]

    def __big_int_reader(vector: Vector, idx: int) -> int:
        return vector.i64_vector.values[idx]

    def __float32_reader(vector: Vector, idx: int) -> float:
        return vector.fp32_vector.values[idx]

    def __float64_reader(vector: Vector, idx: int) -> float:
        return vector.fp64_vector.values[idx]

    def __binary_reader(vector: Vector, idx: int) -> bytes:
        return vector.byte_vector.values[idx]

    def __uuid_reader(vector: Vector, idx: int) -> uuid.UUID:
        return uuid.UUID(bytes=vector.byte_vector.values[idx])

    def __date_reader(vector: Vector, idx: int) -> date:
        iv = vector.i64_vector.values[idx]
        delta = timedelta(days= iv)
        r = (datetime.fromtimestamp(0) + delta).date()
        return r

    match type_id:
        case LogicalDataTypeLogicalDataTypeId.STRING:
            return __string_reader
        case LogicalDataTypeLogicalDataTypeId.BOOL:
            return __bool_reader
        case LogicalDataTypeLogicalDataTypeId.TINY_INT:
            return __int_reader
        case LogicalDataTypeLogicalDataTypeId.SMALL_INT:
            return __int_reader
        case LogicalDataTypeLogicalDataTypeId.INT:
            return __int_reader
        case LogicalDataTypeLogicalDataTypeId.BIG_INT:
            return __big_int_reader
        case LogicalDataTypeLogicalDataTypeId.DOUBLE:
            return __float64_reader
        case LogicalDataTypeLogicalDataTypeId.FLOAT:
            return __float32_reader
        case LogicalDataTypeLogicalDataTypeId.BINARY:
            return __binary_reader
        case LogicalDataTypeLogicalDataTypeId.UUID:
            return __uuid_reader
        case LogicalDataTypeLogicalDataTypeId.DATE:
            return __date_reader
        case _:
            raise MillError(f"No reader for type: {type_id}")

def __get_pyarrow_type(type_id: LogicalDataTypeLogicalDataTypeId):
    match type_id:
        case LogicalDataTypeLogicalDataTypeId.STRING:
            return pa.string(), None
        case LogicalDataTypeLogicalDataTypeId.BOOL:
            return pa.bool_(), None
        case LogicalDataTypeLogicalDataTypeId.TINY_INT:
            return pa.int32(), None
        case LogicalDataTypeLogicalDataTypeId.SMALL_INT:
            return pa.int32(), None
        case LogicalDataTypeLogicalDataTypeId.INT:
            return pa.int32(), None
        case LogicalDataTypeLogicalDataTypeId.BIG_INT:
            return pa.int64(), None
        case LogicalDataTypeLogicalDataTypeId.DOUBLE:
            return pa.float64(), None
        case LogicalDataTypeLogicalDataTypeId.FLOAT:
            return pa.float32(), None
        case LogicalDataTypeLogicalDataTypeId.BINARY:
            return pa.binary(), None
        case LogicalDataTypeLogicalDataTypeId.UUID:
            return pa.string(), lambda x: str(x)
        case LogicalDataTypeLogicalDataTypeId.DATE:
            return pa.date64(), None
        case _:
            raise MillError(f"No pyarrow type mapper for type: {type_id}")

def vector_to_array(type_id: LogicalDataTypeLogicalDataTypeId, vector: Vector, mapper = None):
    reader= __get_reader(type_id)
    r = []
    nulls = vector.nulls.nulls

    def __nonMapping(cidx):
        return reader(vector,cidx)
    def __mapping(cidx):
        return mapper(__nonMapping(cidx))

    read_and_map = lambda x: reader(vector,x)
    if mapper:
        read_and_map = lambda x: mapper(reader(vector,x))

    for idx in range(len(vector.nulls.nulls)):
        if nulls[idx]:
            r.append(None)
        else:
            r.append(read_and_map(idx))
    return r

def vector_block_to_record_batch(vector: VectorBlock) -> RecordBatch:
    arrays = []
    fields = []
    for field in vector.schema.fields:
        arrow_type, mapper = __get_pyarrow_type(field.type.type.type_id)
        ca = vector_to_array(field.type.type.type_id, vector.vectors[field.field_idx], mapper)
        fields.append(pa.field(field.name, arrow_type))
        arrays.append(ca)
    schema = pa.schema(fields)
    return RecordBatch.from_arrays(arrays, schema=schema)

def vector_block_to_pandas(vector: VectorBlock) -> DataFrame:
        record_batch = vector_block_to_record_batch(vector)
        schema = record_batch.schema
        reader = pa.RecordBatchReader.from_batches(schema,[record_batch])
        return reader.read_pandas()

def response_to_record_batch(response: ExecQueryResponse) -> RecordBatch:
    return vector_block_to_record_batch(response.vector)
