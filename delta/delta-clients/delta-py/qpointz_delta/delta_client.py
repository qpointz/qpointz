import asyncio
from grpclib.client import Channel
from libs.qpointzdelta import *


class DeltaClient(object):

    def __init__(self, stub: DeltaServiceStub):
        self._svc = stub
        self._cloop = asyncio.get_event_loop()
        pass

    def _loop(self):
        #return self._svc.channel._loop
        return self._cloop

    def handshake(self) -> HandshakeResponse:
        async def handshake_async() -> HandshakeResponse:
            hr = HandshakeRequest()
            return await self._svc.handshake(hr)
        return self._loop().run_until_complete(handshake_async())

    def list_schemas(self):
        async def listschemasasync():
            ls = ListSchemasRequest()
            return await self._svc.list_schemas(ls)
        return self._loop().run_until_complete(listschemasasync())

    def get_schema(self, schema_name : str):
        async def getschemaasync():
            req = GetSchemaRequest(schema_name= schema_name)
            return await self._svc.get_schema(req)
        return self._loop().run_until_complete(getschemaasync())

    def prepare_sql_statement(self, sql) -> PrepareStatementResponse:
        async def prepare_sql_statement_async():
            sqlstmt = SqlStatement(sql)
            stmt = Statement(sql= sqlstmt)
            req = PrepareStatementRequest(statement=stmt)
            return await self._svc.prepare_statement(req)
        return self._loop().run_until_complete(prepare_sql_statement_async())

    async def execute_async(self, prepared_statement: PreparedStatement):
        req = ExecQueryStreamRequest(statement=prepared_statement)
        async for resp in self._svc.execute_query_stream(req):
            yield resp

    def execute(self, prepared):
        return self._loop().run_until_complete(self.execute_async(prepared_statement=prepared))


    def close(self):
        self._svc.channel.close()


def connect(channel: Channel):
    return create_client(channel)


def create_client(channel: Channel):
    service = DeltaServiceStub(channel)
    return DeltaClient(service)
