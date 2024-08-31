import asyncio
import base64
import enum
from abc import abstractmethod
from asyncio import AbstractEventLoop
from importlib.metadata import metadata
from typing import Optional

from aiostream import stream, await_
from betterproto.grpc.grpclib_client import MetadataLike
from grpclib import GRPCError
from grpclib.client import Channel
import pyarrow as pa
from urllib3 import request

from millclient import utils
from millclient._auth import MillCallCredentials
from millclient.exceptions import MillServerError, MillError
from millclient.proto.io.qpointz.mill import AsyncIterator, ExecQueryResponse, List, MillServiceStub, HandshakeRequest, \
    HandshakeResponse, ListSchemasRequest, ListSchemasResponse, GetSchemaRequest, GetSchemaResponse, ExecSqlRequest, \
    SqlStatement, QueryExecutionConfig


class AuthType(enum.Enum):
    NONE = 0,
    BASIC = 1,
    BEARER = 2
    pass


class MillQuery(object):

    @abstractmethod
    def __event_loop__(self) -> AbstractEventLoop:
        pass

    @abstractmethod
    async def responses(self) -> AsyncIterator[ExecQueryResponse]:
        pass

    async def responses_async(self):
        return await stream.list(self.responses())

    def responses_fetch(self):
        return self.__event_loop__().run_until_complete(self.responses_async())

    async def record_batches(self) -> AsyncIterator[pa.RecordBatch]:
        async for response in self.responses():
            yield utils.response_to_record_batch(response)

    async def record_batches_async(self):
        return await stream.list(self.record_batches())

    def record_batches_fetch(self) -> List[pa.RecordBatch]:
        return self.__event_loop__().run_until_complete(self.record_batches_async())

    async def to_pandas_async(self):
        record_batches = await self.record_batches_async()
        schema = record_batches[0].schema
        reader = pa.RecordBatchReader.from_batches(schema, record_batches)
        return reader.read_pandas()

    def to_pandas(self):
        return self.__event_loop__().run_until_complete(self.to_pandas_async())


class MillSqlQuery(MillQuery):
    pass


class MillClient(object):

    def __init__(self, stub: MillServiceStub, event_loop: AbstractEventLoop | None = None):
        self.__svc = stub
        self.__event_loop = event_loop or asyncio.get_event_loop()
        self.__batch_size = 10000
        pass

    def __enter__(self):
        return self

    def __exit__(self, exc_type, exc_val, exc_tb):
        self.__svc.channel.close()

    def close(self):
        self.__svc.channel.close()

    async def handshake_async(self, req: HandshakeRequest) -> HandshakeResponse:
        return await self.__svc.handshake(handshake_request=req)

    def handshake(self, request: HandshakeRequest = None) -> HandshakeResponse:
        req = request or HandshakeRequest()
        return self.__event_loop.run_until_complete(self.handshake_async(req))

    async def list_schemas_async(self, req):
        return await self.__svc.list_schemas(req)

    def list_schemas(self, request: ListSchemasRequest = None) -> ListSchemasResponse:
        req = request or ListSchemasRequest()
        return self.__event_loop.run_until_complete(self.list_schemas_async(req))

    async def get_schema_async(self, req):
        return await self.__svc.get_schema(req)

    def get_schema(self, request: GetSchemaRequest = None, **kwarg) -> GetSchemaResponse:
        req = request
        if req is None:
            schema_name = kwarg.get('schema_name', None)
            if schema_name is None:
                raise MillError('Missing schema_name parameter')
            req = GetSchemaRequest(schema_name=schema_name)
        try:
            return self.__event_loop.run_until_complete(self.get_schema_async(req))
        except GRPCError as e:
            msg = f"Failed to get schema '{req.schema_name}'.{self.grpcErrorMessage(e)}"
            raise MillServerError(msg, e)

    def __to_sql_request(self, request: ExecSqlRequest = None, **kwarg) -> ExecSqlRequest:
        if request and isinstance(request, ExecSqlRequest):
            return request
        sql = kwarg.get('sql', None)
        if sql is None:
            raise MillError('Missing sql parameter')
        batch_size = int(kwarg.get('batch_size', self.__batch_size))
        return ExecSqlRequest(statement=SqlStatement(sql=str(sql), parameters=[]),
                              config=QueryExecutionConfig(batch_size=batch_size))

    async def exec_sql(self, request: ExecSqlRequest = None, **kwarg: object) -> AsyncIterator[ExecQueryResponse]:
        req = self.__to_sql_request(request, **kwarg)
        async for response in self.__svc.exec_sql(req):
            yield response

    async def exec_sql_async(self, request, **kwarg) -> AsyncIterator[ExecQueryResponse]:
        return await stream.list(self.exec_sql(request=request, **kwarg))

    def exec_sql_fetch(self, request: ExecSqlRequest = None, **kwarg):
        return self.__event_loop.run_until_complete(self.exec_sql_async(request, **kwarg))

    def sql_query(self, request: ExecSqlRequest = None, **kwarg) -> MillSqlQuery:
        req = self.__to_sql_request(request, **kwarg)
        return MillSqlQuery(self, req)
        pass

    def event_loop(self):
        return self.__event_loop

    def grpcErrorMessage(self, error: GRPCError):
        return f"Error code:{error.status}. Message:{error.message}."


class MillSqlQuery(MillQuery):
    def __init__(self, client: MillClient, request: ExecSqlRequest):
        self.__client = client
        self.__request = request
        pass

    async def responses(self) -> AsyncIterator[ExecQueryResponse]:
        async for response in self.__client.exec_sql(request=self.__request):
            yield response

    def __event_loop__(self) -> AbstractEventLoop:
        return self.__client.event_loop()


def create_client(*, channel: Channel = None, creds: MillCallCredentials = None, host:str = "localhost", port: int = 9099, ssl: bool = False, event_loop: AbstractEventLoop = None, metadata: Optional[MetadataLike] = None ) -> MillClient:
    if not channel:
        channel = Channel(host = host, port = port, ssl= ssl)
    if not metadata:
        metadata = {}
    if creds:
        metadata.update(creds.get_metadata())

    stub = MillServiceStub(channel=channel, metadata = metadata)
    el = event_loop or asyncio.get_event_loop()
    return MillClient(stub=stub, event_loop=el)
