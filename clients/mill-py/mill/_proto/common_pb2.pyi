from google.protobuf.internal import containers as _containers
from google.protobuf.internal import enum_type_wrapper as _enum_type_wrapper
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from typing import ClassVar as _ClassVar, Iterable as _Iterable, Mapping as _Mapping, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class ProtocolVersion(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = ()
    UNKNOWN: _ClassVar[ProtocolVersion]
    V1_0: _ClassVar[ProtocolVersion]
UNKNOWN: ProtocolVersion
V1_0: ProtocolVersion

class Schema(_message.Message):
    __slots__ = ("tables",)
    TABLES_FIELD_NUMBER: _ClassVar[int]
    tables: _containers.RepeatedCompositeFieldContainer[Table]
    def __init__(self, tables: _Optional[_Iterable[_Union[Table, _Mapping]]] = ...) -> None: ...

class Table(_message.Message):
    __slots__ = ("schemaName", "name", "tableType", "fields")
    class TableTypeId(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
        __slots__ = ()
        NOT_SPECIFIED_TABLE_TYPE: _ClassVar[Table.TableTypeId]
        TABLE: _ClassVar[Table.TableTypeId]
        VIEW: _ClassVar[Table.TableTypeId]
    NOT_SPECIFIED_TABLE_TYPE: Table.TableTypeId
    TABLE: Table.TableTypeId
    VIEW: Table.TableTypeId
    SCHEMANAME_FIELD_NUMBER: _ClassVar[int]
    NAME_FIELD_NUMBER: _ClassVar[int]
    TABLETYPE_FIELD_NUMBER: _ClassVar[int]
    FIELDS_FIELD_NUMBER: _ClassVar[int]
    schemaName: str
    name: str
    tableType: Table.TableTypeId
    fields: _containers.RepeatedCompositeFieldContainer[Field]
    def __init__(self, schemaName: _Optional[str] = ..., name: _Optional[str] = ..., tableType: _Optional[_Union[Table.TableTypeId, str]] = ..., fields: _Optional[_Iterable[_Union[Field, _Mapping]]] = ...) -> None: ...

class LogicalDataType(_message.Message):
    __slots__ = ("typeId", "precision", "scale")
    class LogicalDataTypeId(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
        __slots__ = ()
        NOT_SPECIFIED_TYPE: _ClassVar[LogicalDataType.LogicalDataTypeId]
        TINY_INT: _ClassVar[LogicalDataType.LogicalDataTypeId]
        SMALL_INT: _ClassVar[LogicalDataType.LogicalDataTypeId]
        INT: _ClassVar[LogicalDataType.LogicalDataTypeId]
        BIG_INT: _ClassVar[LogicalDataType.LogicalDataTypeId]
        BINARY: _ClassVar[LogicalDataType.LogicalDataTypeId]
        BOOL: _ClassVar[LogicalDataType.LogicalDataTypeId]
        DATE: _ClassVar[LogicalDataType.LogicalDataTypeId]
        FLOAT: _ClassVar[LogicalDataType.LogicalDataTypeId]
        DOUBLE: _ClassVar[LogicalDataType.LogicalDataTypeId]
        INTERVAL_DAY: _ClassVar[LogicalDataType.LogicalDataTypeId]
        INTERVAL_YEAR: _ClassVar[LogicalDataType.LogicalDataTypeId]
        STRING: _ClassVar[LogicalDataType.LogicalDataTypeId]
        TIMESTAMP: _ClassVar[LogicalDataType.LogicalDataTypeId]
        TIMESTAMP_TZ: _ClassVar[LogicalDataType.LogicalDataTypeId]
        TIME: _ClassVar[LogicalDataType.LogicalDataTypeId]
        UUID: _ClassVar[LogicalDataType.LogicalDataTypeId]
    NOT_SPECIFIED_TYPE: LogicalDataType.LogicalDataTypeId
    TINY_INT: LogicalDataType.LogicalDataTypeId
    SMALL_INT: LogicalDataType.LogicalDataTypeId
    INT: LogicalDataType.LogicalDataTypeId
    BIG_INT: LogicalDataType.LogicalDataTypeId
    BINARY: LogicalDataType.LogicalDataTypeId
    BOOL: LogicalDataType.LogicalDataTypeId
    DATE: LogicalDataType.LogicalDataTypeId
    FLOAT: LogicalDataType.LogicalDataTypeId
    DOUBLE: LogicalDataType.LogicalDataTypeId
    INTERVAL_DAY: LogicalDataType.LogicalDataTypeId
    INTERVAL_YEAR: LogicalDataType.LogicalDataTypeId
    STRING: LogicalDataType.LogicalDataTypeId
    TIMESTAMP: LogicalDataType.LogicalDataTypeId
    TIMESTAMP_TZ: LogicalDataType.LogicalDataTypeId
    TIME: LogicalDataType.LogicalDataTypeId
    UUID: LogicalDataType.LogicalDataTypeId
    TYPEID_FIELD_NUMBER: _ClassVar[int]
    PRECISION_FIELD_NUMBER: _ClassVar[int]
    SCALE_FIELD_NUMBER: _ClassVar[int]
    typeId: LogicalDataType.LogicalDataTypeId
    precision: int
    scale: int
    def __init__(self, typeId: _Optional[_Union[LogicalDataType.LogicalDataTypeId, str]] = ..., precision: _Optional[int] = ..., scale: _Optional[int] = ...) -> None: ...

class DataType(_message.Message):
    __slots__ = ("type", "nullability")
    class Nullability(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
        __slots__ = ()
        NOT_SPECIFIED_NULL: _ClassVar[DataType.Nullability]
        NULL: _ClassVar[DataType.Nullability]
        NOT_NULL: _ClassVar[DataType.Nullability]
    NOT_SPECIFIED_NULL: DataType.Nullability
    NULL: DataType.Nullability
    NOT_NULL: DataType.Nullability
    TYPE_FIELD_NUMBER: _ClassVar[int]
    NULLABILITY_FIELD_NUMBER: _ClassVar[int]
    type: LogicalDataType
    nullability: DataType.Nullability
    def __init__(self, type: _Optional[_Union[LogicalDataType, _Mapping]] = ..., nullability: _Optional[_Union[DataType.Nullability, str]] = ...) -> None: ...

class Field(_message.Message):
    __slots__ = ("name", "fieldIdx", "type")
    NAME_FIELD_NUMBER: _ClassVar[int]
    FIELDIDX_FIELD_NUMBER: _ClassVar[int]
    TYPE_FIELD_NUMBER: _ClassVar[int]
    name: str
    fieldIdx: int
    type: DataType
    def __init__(self, name: _Optional[str] = ..., fieldIdx: _Optional[int] = ..., type: _Optional[_Union[DataType, _Mapping]] = ...) -> None: ...
