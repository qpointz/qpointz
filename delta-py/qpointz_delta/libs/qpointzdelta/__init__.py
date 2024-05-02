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

from .. import substrait as _substrait__


if TYPE_CHECKING:
    import grpclib.server
    from betterproto.grpc.grpclib_client import MetadataLike
    from grpclib.metadata import Deadline


class ResponseCode(betterproto.Enum):
    OK = 0
    ERROR = 1
    INVALID_REQUEST = 2
    SERVER_ERROR = 3


class ProtocolVersion(betterproto.Enum):
    UNKNOWN = 0
    V1_0 = 1


class TextPlanStatementTextPlanFormat(betterproto.Enum):
    JSON = 0
    YAML = 1


@dataclass(eq=False, repr=False)
class ResponseStatus(betterproto.Message):
    code: "ResponseCode" = betterproto.enum_field(1)
    message: str = betterproto.string_field(2)
    errors: List[str] = betterproto.string_field(3)


@dataclass(eq=False, repr=False)
class Schema(betterproto.Message):
    tables: List["Table"] = betterproto.message_field(1)


@dataclass(eq=False, repr=False)
class Table(betterproto.Message):
    schema_name: str = betterproto.string_field(1)
    name: str = betterproto.string_field(2)
    fields: List["Field"] = betterproto.message_field(3)


@dataclass(eq=False, repr=False)
class Field(betterproto.Message):
    name: str = betterproto.string_field(1)
    index: int = betterproto.uint32_field(2)
    type: "_substrait__.Type" = betterproto.message_field(3)


@dataclass(eq=False, repr=False)
class VectorBlock(betterproto.Message):
    schema: "Table" = betterproto.message_field(1)
    vector_size: int = betterproto.uint32_field(2)
    vectors: List["Vector"] = betterproto.message_field(3)


@dataclass(eq=False, repr=False)
class Vector(betterproto.Message):
    index: int = betterproto.uint32_field(1)
    string_vector: "VectorStringVector" = betterproto.message_field(100, group="data")
    int32_vector: "VectorInt32Vector" = betterproto.message_field(101, group="data")
    int64_vector: "VectorInt64Vector" = betterproto.message_field(102, group="data")
    double_vector: "VectorDoubleVector" = betterproto.message_field(103, group="data")
    float_vector: "VectorFloatVector" = betterproto.message_field(104, group="data")
    bool_vector: "VectorBoolVector" = betterproto.message_field(105, group="data")
    byte_vector: "VectorByteVector" = betterproto.message_field(106, group="data")


@dataclass(eq=False, repr=False)
class VectorStringVector(betterproto.Message):
    values: List[str] = betterproto.string_field(1)
    nulls: List[bool] = betterproto.bool_field(2)


@dataclass(eq=False, repr=False)
class VectorInt32Vector(betterproto.Message):
    values: List[int] = betterproto.sint32_field(1)
    nulls: List[bool] = betterproto.bool_field(2)


@dataclass(eq=False, repr=False)
class VectorInt64Vector(betterproto.Message):
    values: List[int] = betterproto.sint64_field(1)
    nulls: List[bool] = betterproto.bool_field(2)


@dataclass(eq=False, repr=False)
class VectorDoubleVector(betterproto.Message):
    values: List[float] = betterproto.double_field(1)
    nulls: List[bool] = betterproto.bool_field(2)


@dataclass(eq=False, repr=False)
class VectorFloatVector(betterproto.Message):
    values: List[float] = betterproto.float_field(1)
    nulls: List[bool] = betterproto.bool_field(2)


@dataclass(eq=False, repr=False)
class VectorBoolVector(betterproto.Message):
    values: List[bool] = betterproto.bool_field(1)
    nulls: List[bool] = betterproto.bool_field(2)


@dataclass(eq=False, repr=False)
class VectorByteVector(betterproto.Message):
    values: List[bytes] = betterproto.bytes_field(1)
    nulls: List[bool] = betterproto.bool_field(2)


@dataclass(eq=False, repr=False)
class Parameter(betterproto.Message):
    index: int = betterproto.uint32_field(1)
    name: Optional[str] = betterproto.string_field(2, optional=True, group="_name")
    type: "_substrait__.Type" = betterproto.message_field(3)
    boolean_value: bool = betterproto.bool_field(10, group="value")
    string_value: str = betterproto.string_field(11, group="value")
    int32_value: int = betterproto.int32_field(12, group="value")
    int64_value: int = betterproto.int64_field(13, group="value")
    float_value: float = betterproto.float_field(14, group="value")
    double_value: float = betterproto.double_field(15, group="value")


@dataclass(eq=False, repr=False)
class SqlStatement(betterproto.Message):
    statement: str = betterproto.string_field(1)
    parameters: List["Parameter"] = betterproto.message_field(2)


@dataclass(eq=False, repr=False)
class PlanStatement(betterproto.Message):
    plan: "_substrait__.Plan" = betterproto.message_field(1)


@dataclass(eq=False, repr=False)
class TextPlanStatement(betterproto.Message):
    plan: str = betterproto.string_field(1)
    format: "TextPlanStatementTextPlanFormat" = betterproto.enum_field(2)


@dataclass(eq=False, repr=False)
class Statement(betterproto.Message):
    sql: "SqlStatement" = betterproto.message_field(10, group="content")
    plan: "PlanStatement" = betterproto.message_field(20, group="content")
    text: "TextPlanStatement" = betterproto.message_field(30, group="content")


@dataclass(eq=False, repr=False)
class PreparedStatement(betterproto.Message):
    sql: "SqlStatement" = betterproto.message_field(10, group="content")
    plan: "PlanStatement" = betterproto.message_field(20, group="content")


@dataclass(eq=False, repr=False)
class HandshakeRequest(betterproto.Message):
    pass


@dataclass(eq=False, repr=False)
class HandshakeResponse(betterproto.Message):
    status: "ResponseStatus" = betterproto.message_field(1)
    version: "ProtocolVersion" = betterproto.enum_field(2)
    capabilities: "HandshakeResponseCapabilities" = betterproto.message_field(3)
    security: "HandshakeResponseSecurityContext" = betterproto.message_field(4)


@dataclass(eq=False, repr=False)
class HandshakeResponseCapabilities(betterproto.Message):
    support_sql: bool = betterproto.bool_field(3)
    support_plan: bool = betterproto.bool_field(4)
    support_json_plan: bool = betterproto.bool_field(5)


@dataclass(eq=False, repr=False)
class HandshakeResponseSecurityContext(betterproto.Message):
    principal: str = betterproto.string_field(1)


@dataclass(eq=False, repr=False)
class ListSchemasRequest(betterproto.Message):
    pass


@dataclass(eq=False, repr=False)
class ListSchemasResponse(betterproto.Message):
    status: "ResponseStatus" = betterproto.message_field(1)
    schemas: List[str] = betterproto.string_field(2)


@dataclass(eq=False, repr=False)
class GetSchemaRequest(betterproto.Message):
    schema_name: str = betterproto.string_field(2)


@dataclass(eq=False, repr=False)
class GetSchemaResponse(betterproto.Message):
    status: "ResponseStatus" = betterproto.message_field(1)
    schema: "Schema" = betterproto.message_field(2)


@dataclass(eq=False, repr=False)
class PrepareStatementRequest(betterproto.Message):
    statement: "Statement" = betterproto.message_field(1)


@dataclass(eq=False, repr=False)
class PrepareStatementResponse(betterproto.Message):
    status: "ResponseStatus" = betterproto.message_field(1)
    original_statement: "Statement" = betterproto.message_field(2)
    statement: "PreparedStatement" = betterproto.message_field(3)


@dataclass(eq=False, repr=False)
class ExecQueryStreamRequest(betterproto.Message):
    statement: "PreparedStatement" = betterproto.message_field(1)
    batch_size: int = betterproto.uint32_field(2)


@dataclass(eq=False, repr=False)
class ExecQueryResponse(betterproto.Message):
    status: "ResponseStatus" = betterproto.message_field(1)
    vector: "VectorBlock" = betterproto.message_field(101)


class DeltaServiceStub(betterproto.ServiceStub):
    async def handshake(
        self,
        handshake_request: "HandshakeRequest",
        *,
        timeout: Optional[float] = None,
        deadline: Optional["Deadline"] = None,
        metadata: Optional["MetadataLike"] = None
    ) -> "HandshakeResponse":
        return await self._unary_unary(
            "/qpointzdelta.DeltaService/Handshake",
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
            "/qpointzdelta.DeltaService/ListSchemas",
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
            "/qpointzdelta.DeltaService/GetSchema",
            get_schema_request,
            GetSchemaResponse,
            timeout=timeout,
            deadline=deadline,
            metadata=metadata,
        )

    async def prepare_statement(
        self,
        prepare_statement_request: "PrepareStatementRequest",
        *,
        timeout: Optional[float] = None,
        deadline: Optional["Deadline"] = None,
        metadata: Optional["MetadataLike"] = None
    ) -> "PrepareStatementResponse":
        return await self._unary_unary(
            "/qpointzdelta.DeltaService/PrepareStatement",
            prepare_statement_request,
            PrepareStatementResponse,
            timeout=timeout,
            deadline=deadline,
            metadata=metadata,
        )

    async def execute_query_stream(
        self,
        exec_query_stream_request: "ExecQueryStreamRequest",
        *,
        timeout: Optional[float] = None,
        deadline: Optional["Deadline"] = None,
        metadata: Optional["MetadataLike"] = None
    ) -> AsyncIterator["ExecQueryResponse"]:
        async for response in self._unary_stream(
            "/qpointzdelta.DeltaService/ExecuteQueryStream",
            exec_query_stream_request,
            ExecQueryResponse,
            timeout=timeout,
            deadline=deadline,
            metadata=metadata,
        ):
            yield response


class DeltaServiceBase(ServiceBase):

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

    async def prepare_statement(
        self, prepare_statement_request: "PrepareStatementRequest"
    ) -> "PrepareStatementResponse":
        raise grpclib.GRPCError(grpclib.const.Status.UNIMPLEMENTED)

    async def execute_query_stream(
        self, exec_query_stream_request: "ExecQueryStreamRequest"
    ) -> AsyncIterator["ExecQueryResponse"]:
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

    async def __rpc_prepare_statement(
        self,
        stream: "grpclib.server.Stream[PrepareStatementRequest, PrepareStatementResponse]",
    ) -> None:
        request = await stream.recv_message()
        response = await self.prepare_statement(request)
        await stream.send_message(response)

    async def __rpc_execute_query_stream(
        self, stream: "grpclib.server.Stream[ExecQueryStreamRequest, ExecQueryResponse]"
    ) -> None:
        request = await stream.recv_message()
        await self._call_rpc_handler_server_stream(
            self.execute_query_stream,
            stream,
            request,
        )

    def __mapping__(self) -> Dict[str, grpclib.const.Handler]:
        return {
            "/qpointzdelta.DeltaService/Handshake": grpclib.const.Handler(
                self.__rpc_handshake,
                grpclib.const.Cardinality.UNARY_UNARY,
                HandshakeRequest,
                HandshakeResponse,
            ),
            "/qpointzdelta.DeltaService/ListSchemas": grpclib.const.Handler(
                self.__rpc_list_schemas,
                grpclib.const.Cardinality.UNARY_UNARY,
                ListSchemasRequest,
                ListSchemasResponse,
            ),
            "/qpointzdelta.DeltaService/GetSchema": grpclib.const.Handler(
                self.__rpc_get_schema,
                grpclib.const.Cardinality.UNARY_UNARY,
                GetSchemaRequest,
                GetSchemaResponse,
            ),
            "/qpointzdelta.DeltaService/PrepareStatement": grpclib.const.Handler(
                self.__rpc_prepare_statement,
                grpclib.const.Cardinality.UNARY_UNARY,
                PrepareStatementRequest,
                PrepareStatementResponse,
            ),
            "/qpointzdelta.DeltaService/ExecuteQueryStream": grpclib.const.Handler(
                self.__rpc_execute_query_stream,
                grpclib.const.Cardinality.UNARY_STREAM,
                ExecQueryStreamRequest,
                ExecQueryResponse,
            ),
        }
