import asyncio
import enum
from abc import abstractmethod
from asyncio import AbstractEventLoop

from aiostream import stream
from grpclib import GRPCError
from grpclib.client import Channel
import pyarrow as pa

from millclient import utils
from millclient.exceptions import MillServerError, MillError
from millclient.proto.io.qpointz.mill import *


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

    def responses_fetch(self):
        async def exec_sql_fetch_async():
            return await stream.list(self.responses())
            pass
        return self.__event_loop__().run_until_complete(exec_sql_fetch_async())

    async def record_batches(self) -> AsyncIterator[pa.RecordBatch]:
        async for response in self.responses():
            yield utils.response_to_record_batch(response)

    def record_batches_fetch(self) -> List[pa.RecordBatch]:
        async def record_batches_fetch_async():
            return await stream.list(self.record_batches())
        return self.__event_loop__().run_until_complete(record_batches_fetch_async())

    def to_pandas(self):
        record_batches = self.record_batches_fetch()
        schema = record_batches[0].schema
        reader = pa.RecordBatchReader.from_batches(schema, record_batches)
        return reader.read_pandas()



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

    def handshake(self, request: HandshakeRequest = None) -> HandshakeResponse:
        async def handshake_async(req: HandshakeRequest) -> HandshakeResponse:
            return await self.__svc.handshake(req)

        req = request or HandshakeRequest()
        return self.__event_loop.run_until_complete(handshake_async(req))

    def list_schemas(self, request: ListSchemasRequest = None) -> ListSchemasResponse:
        async def list_schemas_async(req):
            return await self.__svc.list_schemas(req)

        req = request or ListSchemasRequest()
        return self.__event_loop.run_until_complete(list_schemas_async(req))

    def get_schema(self, request: GetSchemaRequest = None, **kwarg) -> GetSchemaResponse:
        async def get_schema_async(req):
            return await self.__svc.get_schema(req)

        req = request
        if req is None:
            schema_name = kwarg.get('schema_name', None)
            if schema_name is None:
                raise MillError('Missing schema_name parameter')
            req = GetSchemaRequest(schema_name=schema_name)
        try:
            return self.__event_loop.run_until_complete(get_schema_async(req))
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

    def exec_sql_fetch(self, request: ExecSqlRequest = None, **kwarg):
        async def exec_sql_fetch_async():
            return await stream.list(self.exec_sql(request=request, **kwarg))

        return self.__event_loop.run_until_complete(exec_sql_fetch_async())

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


def create_client(channel: Channel, event_loop: AbstractEventLoop = None) -> MillClient:
    stub = MillServiceStub(channel=channel)
    el = event_loop or asyncio.get_event_loop()
    return MillClient(stub=stub, event_loop=el)
