from substrait import plan_pb2 as _plan_pb2
import common_pb2 as _common_pb2
import statement_pb2 as _statement_pb2
import vector_pb2 as _vector_pb2
from google.protobuf.internal import containers as _containers
from google.protobuf.internal import enum_type_wrapper as _enum_type_wrapper
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from typing import ClassVar as _ClassVar, Iterable as _Iterable, Mapping as _Mapping, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class MetaInfoKey(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = ()
    SEP: _ClassVar[MetaInfoKey]
    DEP: _ClassVar[MetaInfoKey]
SEP: MetaInfoKey
DEP: MetaInfoKey

class HandshakeRequest(_message.Message):
    __slots__ = ()
    def __init__(self) -> None: ...

class HandshakeResponse(_message.Message):
    __slots__ = ("version", "capabilities", "authentication", "metas")
    class Capabilities(_message.Message):
        __slots__ = ("supportSql", "supportResultPaging")
        SUPPORTSQL_FIELD_NUMBER: _ClassVar[int]
        SUPPORTRESULTPAGING_FIELD_NUMBER: _ClassVar[int]
        supportSql: bool
        supportResultPaging: bool
        def __init__(self, supportSql: bool = ..., supportResultPaging: bool = ...) -> None: ...
    class AuthenticationContext(_message.Message):
        __slots__ = ("name",)
        NAME_FIELD_NUMBER: _ClassVar[int]
        name: str
        def __init__(self, name: _Optional[str] = ...) -> None: ...
    class MetasEntry(_message.Message):
        __slots__ = ("key", "value")
        KEY_FIELD_NUMBER: _ClassVar[int]
        VALUE_FIELD_NUMBER: _ClassVar[int]
        key: int
        value: MetaInfoValue
        def __init__(self, key: _Optional[int] = ..., value: _Optional[_Union[MetaInfoValue, _Mapping]] = ...) -> None: ...
    VERSION_FIELD_NUMBER: _ClassVar[int]
    CAPABILITIES_FIELD_NUMBER: _ClassVar[int]
    AUTHENTICATION_FIELD_NUMBER: _ClassVar[int]
    METAS_FIELD_NUMBER: _ClassVar[int]
    version: _common_pb2.ProtocolVersion
    capabilities: HandshakeResponse.Capabilities
    authentication: HandshakeResponse.AuthenticationContext
    metas: _containers.MessageMap[int, MetaInfoValue]
    def __init__(self, version: _Optional[_Union[_common_pb2.ProtocolVersion, str]] = ..., capabilities: _Optional[_Union[HandshakeResponse.Capabilities, _Mapping]] = ..., authentication: _Optional[_Union[HandshakeResponse.AuthenticationContext, _Mapping]] = ..., metas: _Optional[_Mapping[int, MetaInfoValue]] = ...) -> None: ...

class MetaInfoValue(_message.Message):
    __slots__ = ("booleanValue", "stringValue", "int32Value", "int64Value")
    BOOLEANVALUE_FIELD_NUMBER: _ClassVar[int]
    STRINGVALUE_FIELD_NUMBER: _ClassVar[int]
    INT32VALUE_FIELD_NUMBER: _ClassVar[int]
    INT64VALUE_FIELD_NUMBER: _ClassVar[int]
    booleanValue: bool
    stringValue: str
    int32Value: int
    int64Value: int
    def __init__(self, booleanValue: bool = ..., stringValue: _Optional[str] = ..., int32Value: _Optional[int] = ..., int64Value: _Optional[int] = ...) -> None: ...

class ListSchemasRequest(_message.Message):
    __slots__ = ()
    def __init__(self) -> None: ...

class ListSchemasResponse(_message.Message):
    __slots__ = ("schemas",)
    SCHEMAS_FIELD_NUMBER: _ClassVar[int]
    schemas: _containers.RepeatedScalarFieldContainer[str]
    def __init__(self, schemas: _Optional[_Iterable[str]] = ...) -> None: ...

class GetSchemaRequest(_message.Message):
    __slots__ = ("schemaName",)
    SCHEMANAME_FIELD_NUMBER: _ClassVar[int]
    schemaName: str
    def __init__(self, schemaName: _Optional[str] = ...) -> None: ...

class GetSchemaResponse(_message.Message):
    __slots__ = ("schema",)
    SCHEMA_FIELD_NUMBER: _ClassVar[int]
    schema: _common_pb2.Schema
    def __init__(self, schema: _Optional[_Union[_common_pb2.Schema, _Mapping]] = ...) -> None: ...

class ParseSqlRequest(_message.Message):
    __slots__ = ("statement",)
    STATEMENT_FIELD_NUMBER: _ClassVar[int]
    statement: _statement_pb2.SQLStatement
    def __init__(self, statement: _Optional[_Union[_statement_pb2.SQLStatement, _Mapping]] = ...) -> None: ...

class ParseSqlResponse(_message.Message):
    __slots__ = ("plan",)
    PLAN_FIELD_NUMBER: _ClassVar[int]
    plan: _plan_pb2.Plan
    def __init__(self, plan: _Optional[_Union[_plan_pb2.Plan, _Mapping]] = ...) -> None: ...

class QueryExecutionConfig(_message.Message):
    __slots__ = ("fetchSize", "attributes")
    class Attributes(_message.Message):
        __slots__ = ("names", "indexes")
        NAMES_FIELD_NUMBER: _ClassVar[int]
        INDEXES_FIELD_NUMBER: _ClassVar[int]
        names: _containers.RepeatedScalarFieldContainer[str]
        indexes: _containers.RepeatedScalarFieldContainer[int]
        def __init__(self, names: _Optional[_Iterable[str]] = ..., indexes: _Optional[_Iterable[int]] = ...) -> None: ...
    FETCHSIZE_FIELD_NUMBER: _ClassVar[int]
    ATTRIBUTES_FIELD_NUMBER: _ClassVar[int]
    fetchSize: int
    attributes: QueryExecutionConfig.Attributes
    def __init__(self, fetchSize: _Optional[int] = ..., attributes: _Optional[_Union[QueryExecutionConfig.Attributes, _Mapping]] = ...) -> None: ...

class QueryRequest(_message.Message):
    __slots__ = ("config", "plan", "statement")
    CONFIG_FIELD_NUMBER: _ClassVar[int]
    PLAN_FIELD_NUMBER: _ClassVar[int]
    STATEMENT_FIELD_NUMBER: _ClassVar[int]
    config: QueryExecutionConfig
    plan: _plan_pb2.Plan
    statement: _statement_pb2.SQLStatement
    def __init__(self, config: _Optional[_Union[QueryExecutionConfig, _Mapping]] = ..., plan: _Optional[_Union[_plan_pb2.Plan, _Mapping]] = ..., statement: _Optional[_Union[_statement_pb2.SQLStatement, _Mapping]] = ...) -> None: ...

class QueryResultRequest(_message.Message):
    __slots__ = ("pagingId", "fetchSize")
    PAGINGID_FIELD_NUMBER: _ClassVar[int]
    FETCHSIZE_FIELD_NUMBER: _ClassVar[int]
    pagingId: str
    fetchSize: int
    def __init__(self, pagingId: _Optional[str] = ..., fetchSize: _Optional[int] = ...) -> None: ...

class QueryResultResponse(_message.Message):
    __slots__ = ("pagingId", "vector")
    PAGINGID_FIELD_NUMBER: _ClassVar[int]
    VECTOR_FIELD_NUMBER: _ClassVar[int]
    pagingId: str
    vector: _vector_pb2.VectorBlock
    def __init__(self, pagingId: _Optional[str] = ..., vector: _Optional[_Union[_vector_pb2.VectorBlock, _Mapping]] = ...) -> None: ...
