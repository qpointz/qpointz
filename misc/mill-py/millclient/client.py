import asyncio
import enum
from abc import abstractmethod
from asyncio import AbstractEventLoop
from typing import Optional

import aiohttp
import pyarrow as pa
from aiohttp import ClientSession
from aiostream import stream
from betterproto import Message
from betterproto.grpc.grpclib_client import MetadataLike
from grpclib import GRPCError
from grpclib.client import Channel

from millclient import utils
from millclient.auth import MillCallCredentials
from millclient.exceptions import MillServerError, MillError
from millclient.proto.io.qpointz.mill import AsyncIterator, List, HandshakeRequest, \
    HandshakeResponse, ListSchemasRequest, ListSchemasResponse, GetSchemaRequest, GetSchemaResponse, \
    SqlStatement, QueryExecutionConfig, QueryResultResponse, QueryRequest, DataConnectServiceStub


class AuthType(enum.Enum):
    NONE = 0,
    BASIC = 1,
    BEARER = 2

#fakeit
class MillQuery(object):

    @abstractmethod
    def __event_loop__(self) -> AbstractEventLoop:
        pass

    @abstractmethod
    async def responses(self) -> AsyncIterator[QueryResultResponse]:
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

    def __init__(self, event_loop: AbstractEventLoop | None = None):
        self.__event_loop = event_loop or asyncio.get_event_loop()
        self.__fetch_size = 10000

    def __enter__(self):
        return self

    def __exit__(self, exc_type, exc_val, exc_tb):
        self.close()

    @abstractmethod
    def close(self):
        pass

    @abstractmethod
    async def handshake_async(self, req: HandshakeRequest) -> HandshakeResponse:
        pass

    def handshake(self, request: HandshakeRequest = None) -> HandshakeResponse:
        req = request or HandshakeRequest()
        return self.__event_loop.run_until_complete(self.handshake_async(req))

    @abstractmethod
    async def list_schemas_async(self, req):
        pass

    def list_schemas(self, request: ListSchemasRequest = None) -> ListSchemasResponse:
        req = request or ListSchemasRequest()
        return self.__event_loop.run_until_complete(self.list_schemas_async(req))

    @abstractmethod
    async def get_schema_async(self, req):
        pass

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
            msg = f"Failed to get schema '{req.schema_name}'.{self.grpc_error_message(e)}"
            raise MillServerError(msg, e)

    def __to_sql_request(self, request: QueryRequest = None, **kwarg) -> QueryRequest:
        if request and isinstance(request, QueryRequest):
            return request
        sql = kwarg.get('sql', None)
        if sql is None:
            raise MillError('Missing sql parameter')
        fetch_size = int(kwarg.get('fetch_size', self.__fetch_size))
        return QueryRequest(statement=SqlStatement(sql=str(sql), parameters=[]),
                              config=QueryExecutionConfig(fetch_size=fetch_size))

    @abstractmethod
    def exec_query_async_iter(self, request: QueryRequest, **kwarg) -> AsyncIterator[QueryResultResponse]:
        pass

    async def exec_query(self, request: QueryRequest = None, **kwarg: object) -> AsyncIterator[QueryResultResponse]:
        req = self.__to_sql_request(request, **kwarg)
        try:
            iter = self.exec_query_async_iter(req)
            async for response in iter:
                yield response
        except GRPCError as e:
            raise MillServerError(f"Error : {e.status}. Message: {e.message}", e)

    async def exec_query_async(self, request, **kwarg) -> AsyncIterator[QueryResultResponse]:
        return await stream.list(self.exec_query(request=request, **kwarg))

    def exec_query_fetch(self, request: QueryRequest = None, **kwarg):
        return self.__event_loop.run_until_complete(self.exec_query_async(request, **kwarg))

    def sql_query(self, request: QueryRequest = None, **kwarg) -> MillSqlQuery:
        req = self.__to_sql_request(request, **kwarg)
        return MillSqlQuery(self, req)

    def event_loop(self):
        return self.__event_loop

    def grpc_error_message(self, error: GRPCError):
        return f"Error code:{error.status}. Message:{error.message}."


class MillGrpcClient(MillClient):

    def __init__(self, stub: DataConnectServiceStub, event_loop: AbstractEventLoop | None = None):
        super().__init__(event_loop)
        self.__svc = stub

    def close(self):
        self.__svc.channel.close()

    async def handshake_async(self, req: HandshakeRequest) -> HandshakeResponse:
        return await self.__svc.handshake(handshake_request=req)

    async def list_schemas_async(self, req):
        return await self.__svc.list_schemas(req)

    def exec_query_async_iter(self, request: QueryRequest, **kwarg) -> AsyncIterator[QueryResultResponse]:
        return self.__svc.exec_query(request)

    async def get_schema_async(self, req):
        return await self.__svc.get_schema(req)

class MillHttpSession(object):
    def __init__(self, session: ClientSession, base_url: str, event_loop: AbstractEventLoop ):
        self.__session = session
        self.__base_url = base_url
        self.__event_loop = event_loop

    def close(self):
        self.__event_loop.run_until_complete(self.__session.close())

    async def post(self, command: str, req: Message, res: Message):
        resp = await self.__session.post(f"{self.__base_url}{command}", data=req.to_json())
        if resp.status != 200:
            text = await resp.text()
            ex = Exception(
                f"Error : {resp.status}. Message: {text}. Command: {command}. Request: {req.to_json()}")
            raise MillServerError(f"Failed to execute command '{command}'", ex)
        cnt = await resp.content.read()
        return res.parse(cnt)


class MillHttpClient(MillClient):

    def __init__(self, *, event_loop: AbstractEventLoop | None = None, ssl:bool, host:str, port:int, base_path:str, headers:MetadataLike):
        super().__init__(event_loop)
        protoc = "https" if ssl else "http"
        portsfx = f":{port}" if port else ""
        if not base_path.endswith("/"):
            base_path += "/"
        if not base_path.startswith("/"):
            base_path = "/" + base_path
        self.__url = f"{protoc}://{host}{portsfx}{base_path}"
        headers.update([
            ('Content-Type', 'application/json'),
            ('Accept', 'application/x-protobuf')
        ])
        sess = aiohttp.ClientSession(headers=headers, raise_for_status= False, loop= event_loop)
        self.__session = MillHttpSession(session=sess, base_url=self.__url, event_loop = event_loop)

    def get_base_url(self):
        return f"{self.__url}"

    def close(self):
        self.__session.close()


    async def handshake_async(self, req: HandshakeRequest) -> HandshakeResponse:
        return await self.__session.post("Handshake", req, HandshakeResponse())

    async def list_schemas_async(self, req: ListSchemasRequest) -> ListSchemasResponse:
        return await self.__session.post("ListSchemas", req, ListSchemasResponse())

    async def get_schema_async(self, req: GetSchemaRequest) -> GetSchemaResponse:
        return await self.__session.post("GetSchema", req, GetSchemaResponse())

    def exec_query_async_iter(self, request: QueryRequest, **kwarg) -> AsyncIterator[QueryResultResponse]:
        class PagingIterator(AsyncIterator[QueryResultResponse]):
            def __init__(self, session: MillHttpSession, request: QueryRequest, **kwarg):
                self.__session = session
                self.__did_next = False
                self.__request = request
                self.__did_next:bool = False
                self.__pagingId:str = None

            def __aiter__(self):
                return self

            async def __anext__(self):
                if not self.__did_next:
                    resp = await self.__session.post("SubmitQuery", self.__request, QueryResultResponse())
                    self.__did_next = True
                    self.__pagingId = resp.paging_id
                    return resp
                if not self.__pagingId:
                    raise StopAsyncIteration
                req = QueryResultResponse()
                req.paging_id = self.__pagingId
                resp = await self.__session.post("FetchQueryResult", req, QueryResultResponse())
                if not resp.vector:
                    self.__pagingId = None
                    raise StopAsyncIteration
                self.__pagingId = resp.paging_id
                return resp

        return PagingIterator(self.__session, request, **kwarg)


class MillSqlQuery(MillQuery):
    def __init__(self, client: MillClient, request: QueryRequest):
        self.__client = client
        self.__request = request

    async def responses(self) -> AsyncIterator[QueryResultResponse]:
        async for response in self.__client.exec_query(request=self.__request):
            yield response

    def __event_loop__(self) -> AbstractEventLoop:
        return self.__client.event_loop()

from urllib.parse import urlparse

def create_client(*, channel: Channel = None, creds: MillCallCredentials = None,
                  url:str = None, protocol:str = "grpc", host:str = None, port: int = None, base_path:str="/services/jet/",
                  ssl: bool = False, event_loop: AbstractEventLoop = None, metadata: Optional[MetadataLike] = None ) -> MillClient:
    if host is None:
        parsed = urlparse(url)
        host = parsed.hostname
        port = parsed.port or port
        base_path = parsed.path or base_path
        protocol = (parsed.scheme or protocol).lower()
        if protocol == 'https':
            protocol = 'http'
            ssl = True
        elif protocol == 'http':
            protocol = 'http'
            ssl = False

    el = event_loop or asyncio.get_event_loop()
    if not metadata:
        metadata = {}

    def create_grpc_client( el:AbstractEventLoop, channel: Channel, creds: MillCallCredentials, metadata: MetadataLike):
        if not channel:
            channel = Channel(host = host, port = port, ssl= ssl)
        if creds:
            metadata.update(creds.get_metadata())
        stub = DataConnectServiceStub(channel=channel, metadata = metadata)
        return MillGrpcClient(stub=stub, event_loop=el)

    def create_http_client(el:AbstractEventLoop, ssl: bool, host: str, port: int, base_path: str, metadata: MetadataLike):
        if creds:
            metadata.update(creds.get_headers())
        return MillHttpClient(event_loop= el, ssl = ssl, host = host, port = port, base_path = base_path, headers = metadata )

    if protocol.lower() == "grpc":
        return create_grpc_client(el, channel, creds, metadata)

    if protocol.lower() == "http":
        return create_http_client(el, ssl, host, port, base_path, metadata)

    raise MillError(f"Unsupported protocol:{protocol}")
