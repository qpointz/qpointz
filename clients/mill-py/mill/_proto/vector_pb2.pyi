import common_pb2 as _common_pb2
from google.protobuf.internal import containers as _containers
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from typing import ClassVar as _ClassVar, Iterable as _Iterable, Mapping as _Mapping, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class VectorBlockSchema(_message.Message):
    __slots__ = ("fields",)
    FIELDS_FIELD_NUMBER: _ClassVar[int]
    fields: _containers.RepeatedCompositeFieldContainer[_common_pb2.Field]
    def __init__(self, fields: _Optional[_Iterable[_Union[_common_pb2.Field, _Mapping]]] = ...) -> None: ...

class VectorBlock(_message.Message):
    __slots__ = ("schema", "vectorSize", "vectors")
    SCHEMA_FIELD_NUMBER: _ClassVar[int]
    VECTORSIZE_FIELD_NUMBER: _ClassVar[int]
    VECTORS_FIELD_NUMBER: _ClassVar[int]
    schema: VectorBlockSchema
    vectorSize: int
    vectors: _containers.RepeatedCompositeFieldContainer[Vector]
    def __init__(self, schema: _Optional[_Union[VectorBlockSchema, _Mapping]] = ..., vectorSize: _Optional[int] = ..., vectors: _Optional[_Iterable[_Union[Vector, _Mapping]]] = ...) -> None: ...

class Vector(_message.Message):
    __slots__ = ("fieldIdx", "nulls", "stringVector", "i32Vector", "i64Vector", "fp64Vector", "fp32Vector", "boolVector", "byteVector")
    class NullsVector(_message.Message):
        __slots__ = ("nulls",)
        NULLS_FIELD_NUMBER: _ClassVar[int]
        nulls: _containers.RepeatedScalarFieldContainer[bool]
        def __init__(self, nulls: _Optional[_Iterable[bool]] = ...) -> None: ...
    class StringVector(_message.Message):
        __slots__ = ("values",)
        VALUES_FIELD_NUMBER: _ClassVar[int]
        values: _containers.RepeatedScalarFieldContainer[str]
        def __init__(self, values: _Optional[_Iterable[str]] = ...) -> None: ...
    class I32Vector(_message.Message):
        __slots__ = ("values",)
        VALUES_FIELD_NUMBER: _ClassVar[int]
        values: _containers.RepeatedScalarFieldContainer[int]
        def __init__(self, values: _Optional[_Iterable[int]] = ...) -> None: ...
    class I64Vector(_message.Message):
        __slots__ = ("values",)
        VALUES_FIELD_NUMBER: _ClassVar[int]
        values: _containers.RepeatedScalarFieldContainer[int]
        def __init__(self, values: _Optional[_Iterable[int]] = ...) -> None: ...
    class FP64Vector(_message.Message):
        __slots__ = ("values",)
        VALUES_FIELD_NUMBER: _ClassVar[int]
        values: _containers.RepeatedScalarFieldContainer[float]
        def __init__(self, values: _Optional[_Iterable[float]] = ...) -> None: ...
    class FP32Vector(_message.Message):
        __slots__ = ("values",)
        VALUES_FIELD_NUMBER: _ClassVar[int]
        values: _containers.RepeatedScalarFieldContainer[float]
        def __init__(self, values: _Optional[_Iterable[float]] = ...) -> None: ...
    class BoolVector(_message.Message):
        __slots__ = ("values",)
        VALUES_FIELD_NUMBER: _ClassVar[int]
        values: _containers.RepeatedScalarFieldContainer[bool]
        def __init__(self, values: _Optional[_Iterable[bool]] = ...) -> None: ...
    class BytesVector(_message.Message):
        __slots__ = ("values",)
        VALUES_FIELD_NUMBER: _ClassVar[int]
        values: _containers.RepeatedScalarFieldContainer[bytes]
        def __init__(self, values: _Optional[_Iterable[bytes]] = ...) -> None: ...
    FIELDIDX_FIELD_NUMBER: _ClassVar[int]
    NULLS_FIELD_NUMBER: _ClassVar[int]
    STRINGVECTOR_FIELD_NUMBER: _ClassVar[int]
    I32VECTOR_FIELD_NUMBER: _ClassVar[int]
    I64VECTOR_FIELD_NUMBER: _ClassVar[int]
    FP64VECTOR_FIELD_NUMBER: _ClassVar[int]
    FP32VECTOR_FIELD_NUMBER: _ClassVar[int]
    BOOLVECTOR_FIELD_NUMBER: _ClassVar[int]
    BYTEVECTOR_FIELD_NUMBER: _ClassVar[int]
    fieldIdx: int
    nulls: Vector.NullsVector
    stringVector: Vector.StringVector
    i32Vector: Vector.I32Vector
    i64Vector: Vector.I64Vector
    fp64Vector: Vector.FP64Vector
    fp32Vector: Vector.FP32Vector
    boolVector: Vector.BoolVector
    byteVector: Vector.BytesVector
    def __init__(self, fieldIdx: _Optional[int] = ..., nulls: _Optional[_Union[Vector.NullsVector, _Mapping]] = ..., stringVector: _Optional[_Union[Vector.StringVector, _Mapping]] = ..., i32Vector: _Optional[_Union[Vector.I32Vector, _Mapping]] = ..., i64Vector: _Optional[_Union[Vector.I64Vector, _Mapping]] = ..., fp64Vector: _Optional[_Union[Vector.FP64Vector, _Mapping]] = ..., fp32Vector: _Optional[_Union[Vector.FP32Vector, _Mapping]] = ..., boolVector: _Optional[_Union[Vector.BoolVector, _Mapping]] = ..., byteVector: _Optional[_Union[Vector.BytesVector, _Mapping]] = ...) -> None: ...
