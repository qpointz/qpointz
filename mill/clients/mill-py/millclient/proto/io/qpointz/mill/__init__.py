# Generated by the protocol buffer compiler.  DO NOT EDIT!
# sources: common.proto, service.proto, statement.proto, vector.proto
# plugin: python-betterproto
# This file has been @generated

from dataclasses import dataclass
from typing import (
    TYPE_CHECKING,
    AsyncIterator,
    Dict,
    List,
    Optional,
)

import betterproto
import grpclib
from betterproto.grpc.grpclib_server import ServiceBase

from .... import substrait as ___substrait__


if TYPE_CHECKING:
    import grpclib.server
    from betterproto.grpc.grpclib_client import MetadataLike
    from grpclib.metadata import Deadline


class ProtocolVersion(betterproto.Enum):
    UNKNOWN = 0
    V1_0 = 1


class TableTableTypeId(betterproto.Enum):
    NOT_SPECIFIED_TABLE_TYPE = 0
    TABLE = 1
    VIEW = 2


class LogicalDataTypeLogicalDataTypeId(betterproto.Enum):
    NOT_SPECIFIED_TYPE = 0
    TINY_INT = 1
    SMALL_INT = 2
    INT = 3
    BIG_INT = 4
    BINARY = 5
    BOOL = 6
    DATE = 7
    FLOAT = 8
    DOUBLE = 9
    INTERVAL_DAY = 10
    INTERVAL_YEAR = 11
    STRING = 12
    TIMESTAMP = 13
    TIMESTAMP_TZ = 14
    TIME = 15
    UUID = 16


class DataTypeNullability(betterproto.Enum):
    NOT_SPECIFIED_NULL = 0
    NULL = 1
    NOT_NULL = 2


class TextPlanStatementTextPlanFormat(betterproto.Enum):
    JSON = 0
    YAML = 1


class MetaInfoKey(betterproto.Enum):
    SEP = 0
    DEP = 1


@dataclass(eq=False, repr=False)
class Schema(betterproto.Message):
    tables: List["Table"] = betterproto.message_field(1)


@dataclass(eq=False, repr=False)
class Table(betterproto.Message):
    schema_name: str = betterproto.string_field(1)
    name: str = betterproto.string_field(2)
    table_type: "TableTableTypeId" = betterproto.enum_field(3)
    fields: List["Field"] = betterproto.message_field(4)


@dataclass(eq=False, repr=False)
class LogicalDataType(betterproto.Message):
    type_id: "LogicalDataTypeLogicalDataTypeId" = betterproto.enum_field(2)
    precision: int = betterproto.int32_field(3)
    scale: int = betterproto.int32_field(4)


@dataclass(eq=False, repr=False)
class DataType(betterproto.Message):
    type: "LogicalDataType" = betterproto.message_field(1)
    nullability: "DataTypeNullability" = betterproto.enum_field(2)


@dataclass(eq=False, repr=False)
class Field(betterproto.Message):
    name: str = betterproto.string_field(1)
    field_idx: int = betterproto.uint32_field(2)
    type: "DataType" = betterproto.message_field(3)


@dataclass(eq=False, repr=False)
class Parameter(betterproto.Message):
    index: int = betterproto.uint32_field(1)
    name: Optional[str] = betterproto.string_field(2, optional=True)
    type: "DataType" = betterproto.message_field(3)
    boolean_value: bool = betterproto.bool_field(10, group="value")
    string_value: str = betterproto.string_field(11, group="value")
    int32_value: int = betterproto.int32_field(12, group="value")
    int64_value: int = betterproto.int64_field(13, group="value")
    float_value: float = betterproto.float_field(14, group="value")
    double_value: float = betterproto.double_field(15, group="value")


@dataclass(eq=False, repr=False)
class SqlStatement(betterproto.Message):
    sql: str = betterproto.string_field(1)
    parameters: List["Parameter"] = betterproto.message_field(2)


@dataclass(eq=False, repr=False)
class PlanStatement(betterproto.Message):
    plan: "___substrait__.Plan" = betterproto.message_field(1)


@dataclass(eq=False, repr=False)
class TextPlanStatement(betterproto.Message):
    plan: str = betterproto.string_field(1)
    format: "TextPlanStatementTextPlanFormat" = betterproto.enum_field(2)


@dataclass(eq=False, repr=False)
class VectorBlockSchema(betterproto.Message):
    fields: List["Field"] = betterproto.message_field(3)


@dataclass(eq=False, repr=False)
class VectorBlock(betterproto.Message):
    schema: "VectorBlockSchema" = betterproto.message_field(1)
    vector_size: int = betterproto.uint32_field(2)
    vectors: List["Vector"] = betterproto.message_field(3)


@dataclass(eq=False, repr=False)
class Vector(betterproto.Message):
    field_idx: int = betterproto.uint32_field(1)
    nulls: "VectorNullsVector" = betterproto.message_field(2)
    string_vector: "VectorStringVector" = betterproto.message_field(100, group="values")
    i32_vector: "VectorI32Vector" = betterproto.message_field(101, group="values")
    i64_vector: "VectorI64Vector" = betterproto.message_field(102, group="values")
    fp64_vector: "VectorFp64Vector" = betterproto.message_field(103, group="values")
    fp32_vector: "VectorFp32Vector" = betterproto.message_field(104, group="values")
    bool_vector: "VectorBoolVector" = betterproto.message_field(105, group="values")
    byte_vector: "VectorBytesVector" = betterproto.message_field(106, group="values")


@dataclass(eq=False, repr=False)
class VectorNullsVector(betterproto.Message):
    nulls: List[bool] = betterproto.bool_field(2)


@dataclass(eq=False, repr=False)
class VectorStringVector(betterproto.Message):
    values: List[str] = betterproto.string_field(1)


@dataclass(eq=False, repr=False)
class VectorI32Vector(betterproto.Message):
    values: List[int] = betterproto.int32_field(1)


@dataclass(eq=False, repr=False)
class VectorI64Vector(betterproto.Message):
    values: List[int] = betterproto.int64_field(1)


@dataclass(eq=False, repr=False)
class VectorFp64Vector(betterproto.Message):
    values: List[float] = betterproto.double_field(1)


@dataclass(eq=False, repr=False)
class VectorFp32Vector(betterproto.Message):
    values: List[float] = betterproto.float_field(1)


@dataclass(eq=False, repr=False)
class VectorBoolVector(betterproto.Message):
    values: List[bool] = betterproto.bool_field(1)


@dataclass(eq=False, repr=False)
class VectorBytesVector(betterproto.Message):
    values: List[bytes] = betterproto.bytes_field(1)


@dataclass(eq=False, repr=False)
class HandshakeRequest(betterproto.Message):
    pass


@dataclass(eq=False, repr=False)
class HandshakeResponse(betterproto.Message):
    version: "ProtocolVersion" = betterproto.enum_field(2)
    capabilities: "HandshakeResponseCapabilities" = betterproto.message_field(3)
    authentication: "HandshakeResponseAuthenticationContext" = (
        betterproto.message_field(4)
    )
    metas: Dict[int, "MetaInfoValue"] = betterproto.map_field(
        1, betterproto.TYPE_INT32, betterproto.TYPE_MESSAGE
    )


@dataclass(eq=False, repr=False)
class HandshakeResponseCapabilities(betterproto.Message):
    support_sql: bool = betterproto.bool_field(1)
    support_result_paging: bool = betterproto.bool_field(2)


@dataclass(eq=False, repr=False)
class HandshakeResponseAuthenticationContext(betterproto.Message):
    name: str = betterproto.string_field(1)


@dataclass(eq=False, repr=False)
class MetaInfoValue(betterproto.Message):
    boolean_value: bool = betterproto.bool_field(10, group="value")
    string_value: str = betterproto.string_field(11, group="value")
    int32_value: int = betterproto.int32_field(12, group="value")
    int64_value: int = betterproto.int64_field(13, group="value")


@dataclass(eq=False, repr=False)
class ListSchemasRequest(betterproto.Message):
    pass


@dataclass(eq=False, repr=False)
class ListSchemasResponse(betterproto.Message):
    schemas: List[str] = betterproto.string_field(2)


@dataclass(eq=False, repr=False)
class GetSchemaRequest(betterproto.Message):
    schema_name: str = betterproto.string_field(2)


@dataclass(eq=False, repr=False)
class GetSchemaResponse(betterproto.Message):
    schema: "Schema" = betterproto.message_field(2)


@dataclass(eq=False, repr=False)
class ParseSqlRequest(betterproto.Message):
    statement: "SqlStatement" = betterproto.message_field(2)


@dataclass(eq=False, repr=False)
class ParseSqlResponse(betterproto.Message):
    plan: Optional["___substrait__.Plan"] = betterproto.message_field(3, optional=True)


@dataclass(eq=False, repr=False)
class QueryExecutionConfig(betterproto.Message):
    fetch_size: int = betterproto.int32_field(1)
    attributes: Optional["QueryExecutionConfigAttributes"] = betterproto.message_field(
        2, optional=True
    )


@dataclass(eq=False, repr=False)
class QueryExecutionConfigAttributes(betterproto.Message):
    names: List[str] = betterproto.string_field(1)
    indexes: List[int] = betterproto.int32_field(2)


@dataclass(eq=False, repr=False)
class ExecPlanRequest(betterproto.Message):
    config: "QueryExecutionConfig" = betterproto.message_field(1)
    plan: "___substrait__.Plan" = betterproto.message_field(2)


@dataclass(eq=False, repr=False)
class ExecSqlRequest(betterproto.Message):
    config: "QueryExecutionConfig" = betterproto.message_field(1)
    statement: "SqlStatement" = betterproto.message_field(2)


@dataclass(eq=False, repr=False)
class ExecQueryResponse(betterproto.Message):
    paging_id: Optional[str] = betterproto.string_field(1, optional=True)
    vector: "VectorBlock" = betterproto.message_field(2)


@dataclass(eq=False, repr=False)
class GetNextPageRequest(betterproto.Message):
    paging_id: str = betterproto.string_field(1)


class MillServiceStub(betterproto.ServiceStub):
    async def handshake(
        self,
        handshake_request: "HandshakeRequest",
        *,
        timeout: Optional[float] = None,
        deadline: Optional["Deadline"] = None,
        metadata: Optional["MetadataLike"] = None
    ) -> "HandshakeResponse":
        return await self._unary_unary(
            "/io.qpointz.mill.MillService/Handshake",
            handshake_request,
            HandshakeResponse,
            timeout=timeout,
            deadline=deadline,
            metadata=metadata,
        )

    async def list_schemas(
        self,
        list_schemas_request: "ListSchemasRequest",
        *,
        timeout: Optional[float] = None,
        deadline: Optional["Deadline"] = None,
        metadata: Optional["MetadataLike"] = None
    ) -> "ListSchemasResponse":
        return await self._unary_unary(
            "/io.qpointz.mill.MillService/ListSchemas",
            list_schemas_request,
            ListSchemasResponse,
            timeout=timeout,
            deadline=deadline,
            metadata=metadata,
        )

    async def get_schema(
        self,
        get_schema_request: "GetSchemaRequest",
        *,
        timeout: Optional[float] = None,
        deadline: Optional["Deadline"] = None,
        metadata: Optional["MetadataLike"] = None
    ) -> "GetSchemaResponse":
        return await self._unary_unary(
            "/io.qpointz.mill.MillService/GetSchema",
            get_schema_request,
            GetSchemaResponse,
            timeout=timeout,
            deadline=deadline,
            metadata=metadata,
        )

    async def exec_plan(
        self,
        exec_plan_request: "ExecPlanRequest",
        *,
        timeout: Optional[float] = None,
        deadline: Optional["Deadline"] = None,
        metadata: Optional["MetadataLike"] = None
    ) -> AsyncIterator[ExecQueryResponse]:
        async for response in self._unary_stream(
            "/io.qpointz.mill.MillService/ExecPlan",
            exec_plan_request,
            ExecQueryResponse,
            timeout=timeout,
            deadline=deadline,
            metadata=metadata,
        ):
            yield response

    async def parse_sql(
        self,
        parse_sql_request: "ParseSqlRequest",
        *,
        timeout: Optional[float] = None,
        deadline: Optional["Deadline"] = None,
        metadata: Optional["MetadataLike"] = None
    ) -> "ParseSqlResponse":
        return await self._unary_unary(
            "/io.qpointz.mill.MillService/ParseSql",
            parse_sql_request,
            ParseSqlResponse,
            timeout=timeout,
            deadline=deadline,
            metadata=metadata,
        )

    async def exec_sql(
        self,
        exec_sql_request: "ExecSqlRequest",
        *,
        timeout: Optional[float] = None,
        deadline: Optional["Deadline"] = None,
        metadata: Optional["MetadataLike"] = None
    ) -> AsyncIterator[ExecQueryResponse]:
        async for response in self._unary_stream(
            "/io.qpointz.mill.MillService/ExecSql",
            exec_sql_request,
            ExecQueryResponse,
            timeout=timeout,
            deadline=deadline,
            metadata=metadata,
        ):
            yield response


class MillServiceBase(ServiceBase):

    async def handshake(
        self, handshake_request: "HandshakeRequest"
    ) -> "HandshakeResponse":
        raise grpclib.GRPCError(grpclib.const.Status.UNIMPLEMENTED)

    async def list_schemas(
        self, list_schemas_request: "ListSchemasRequest"
    ) -> "ListSchemasResponse":
        raise grpclib.GRPCError(grpclib.const.Status.UNIMPLEMENTED)

    async def get_schema(
        self, get_schema_request: "GetSchemaRequest"
    ) -> "GetSchemaResponse":
        raise grpclib.GRPCError(grpclib.const.Status.UNIMPLEMENTED)

    async def exec_plan(
        self, exec_plan_request: "ExecPlanRequest"
    ) -> AsyncIterator[ExecQueryResponse]:
        raise grpclib.GRPCError(grpclib.const.Status.UNIMPLEMENTED)
        yield ExecQueryResponse()

    async def parse_sql(
        self, parse_sql_request: "ParseSqlRequest"
    ) -> "ParseSqlResponse":
        raise grpclib.GRPCError(grpclib.const.Status.UNIMPLEMENTED)

    async def exec_sql(
        self, exec_sql_request: "ExecSqlRequest"
    ) -> AsyncIterator[ExecQueryResponse]:
        raise grpclib.GRPCError(grpclib.const.Status.UNIMPLEMENTED)
        yield ExecQueryResponse()

    async def __rpc_handshake(
        self, stream: "grpclib.server.Stream[HandshakeRequest, HandshakeResponse]"
    ) -> None:
        request = await stream.recv_message()
        response = await self.handshake(request)
        await stream.send_message(response)

    async def __rpc_list_schemas(
        self, stream: "grpclib.server.Stream[ListSchemasRequest, ListSchemasResponse]"
    ) -> None:
        request = await stream.recv_message()
        response = await self.list_schemas(request)
        await stream.send_message(response)

    async def __rpc_get_schema(
        self, stream: "grpclib.server.Stream[GetSchemaRequest, GetSchemaResponse]"
    ) -> None:
        request = await stream.recv_message()
        response = await self.get_schema(request)
        await stream.send_message(response)

    async def __rpc_exec_plan(
        self, stream: "grpclib.server.Stream[ExecPlanRequest, ExecQueryResponse]"
    ) -> None:
        request = await stream.recv_message()
        await self._call_rpc_handler_server_stream(
            self.exec_plan,
            stream,
            request,
        )

    async def __rpc_parse_sql(
        self, stream: "grpclib.server.Stream[ParseSqlRequest, ParseSqlResponse]"
    ) -> None:
        request = await stream.recv_message()
        response = await self.parse_sql(request)
        await stream.send_message(response)

    async def __rpc_exec_sql(
        self, stream: "grpclib.server.Stream[ExecSqlRequest, ExecQueryResponse]"
    ) -> None:
        request = await stream.recv_message()
        await self._call_rpc_handler_server_stream(
            self.exec_sql,
            stream,
            request,
        )

    def __mapping__(self) -> Dict[str, grpclib.const.Handler]:
        return {
            "/io.qpointz.mill.MillService/Handshake": grpclib.const.Handler(
                self.__rpc_handshake,
                grpclib.const.Cardinality.UNARY_UNARY,
                HandshakeRequest,
                HandshakeResponse,
            ),
            "/io.qpointz.mill.MillService/ListSchemas": grpclib.const.Handler(
                self.__rpc_list_schemas,
                grpclib.const.Cardinality.UNARY_UNARY,
                ListSchemasRequest,
                ListSchemasResponse,
            ),
            "/io.qpointz.mill.MillService/GetSchema": grpclib.const.Handler(
                self.__rpc_get_schema,
                grpclib.const.Cardinality.UNARY_UNARY,
                GetSchemaRequest,
                GetSchemaResponse,
            ),
            "/io.qpointz.mill.MillService/ExecPlan": grpclib.const.Handler(
                self.__rpc_exec_plan,
                grpclib.const.Cardinality.UNARY_STREAM,
                ExecPlanRequest,
                ExecQueryResponse,
            ),
            "/io.qpointz.mill.MillService/ParseSql": grpclib.const.Handler(
                self.__rpc_parse_sql,
                grpclib.const.Cardinality.UNARY_UNARY,
                ParseSqlRequest,
                ParseSqlResponse,
            ),
            "/io.qpointz.mill.MillService/ExecSql": grpclib.const.Handler(
                self.__rpc_exec_sql,
                grpclib.const.Cardinality.UNARY_STREAM,
                ExecSqlRequest,
                ExecQueryResponse,
            ),
        }
