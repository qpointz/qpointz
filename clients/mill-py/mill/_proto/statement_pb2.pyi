from substrait import plan_pb2 as _plan_pb2
import common_pb2 as _common_pb2
from google.protobuf.internal import containers as _containers
from google.protobuf.internal import enum_type_wrapper as _enum_type_wrapper
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from typing import ClassVar as _ClassVar, Iterable as _Iterable, Mapping as _Mapping, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class Parameter(_message.Message):
    __slots__ = ("index", "name", "type", "booleanValue", "stringValue", "int32Value", "int64Value", "floatValue", "doubleValue")
    INDEX_FIELD_NUMBER: _ClassVar[int]
    NAME_FIELD_NUMBER: _ClassVar[int]
    TYPE_FIELD_NUMBER: _ClassVar[int]
    BOOLEANVALUE_FIELD_NUMBER: _ClassVar[int]
    STRINGVALUE_FIELD_NUMBER: _ClassVar[int]
    INT32VALUE_FIELD_NUMBER: _ClassVar[int]
    INT64VALUE_FIELD_NUMBER: _ClassVar[int]
    FLOATVALUE_FIELD_NUMBER: _ClassVar[int]
    DOUBLEVALUE_FIELD_NUMBER: _ClassVar[int]
    index: int
    name: str
    type: _common_pb2.DataType
    booleanValue: bool
    stringValue: str
    int32Value: int
    int64Value: int
    floatValue: float
    doubleValue: float
    def __init__(self, index: _Optional[int] = ..., name: _Optional[str] = ..., type: _Optional[_Union[_common_pb2.DataType, _Mapping]] = ..., booleanValue: bool = ..., stringValue: _Optional[str] = ..., int32Value: _Optional[int] = ..., int64Value: _Optional[int] = ..., floatValue: _Optional[float] = ..., doubleValue: _Optional[float] = ...) -> None: ...

class SQLStatement(_message.Message):
    __slots__ = ("sql", "parameters")
    SQL_FIELD_NUMBER: _ClassVar[int]
    PARAMETERS_FIELD_NUMBER: _ClassVar[int]
    sql: str
    parameters: _containers.RepeatedCompositeFieldContainer[Parameter]
    def __init__(self, sql: _Optional[str] = ..., parameters: _Optional[_Iterable[_Union[Parameter, _Mapping]]] = ...) -> None: ...

class PlanStatement(_message.Message):
    __slots__ = ("plan",)
    PLAN_FIELD_NUMBER: _ClassVar[int]
    plan: _plan_pb2.Plan
    def __init__(self, plan: _Optional[_Union[_plan_pb2.Plan, _Mapping]] = ...) -> None: ...

class TextPlanStatement(_message.Message):
    __slots__ = ("plan", "format")
    class TextPlanFormat(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
        __slots__ = ()
        JSON: _ClassVar[TextPlanStatement.TextPlanFormat]
        YAML: _ClassVar[TextPlanStatement.TextPlanFormat]
    JSON: TextPlanStatement.TextPlanFormat
    YAML: TextPlanStatement.TextPlanFormat
    PLAN_FIELD_NUMBER: _ClassVar[int]
    FORMAT_FIELD_NUMBER: _ClassVar[int]
    plan: str
    format: TextPlanStatement.TextPlanFormat
    def __init__(self, plan: _Optional[str] = ..., format: _Optional[_Union[TextPlanStatement.TextPlanFormat, str]] = ...) -> None: ...
