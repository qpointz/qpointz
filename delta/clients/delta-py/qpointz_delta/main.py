import asyncio
import sys

from delta_client import create_client
from grpclib.client import Channel

from libs.qpointzdelta import DeltaServiceStub, HandshakeRequest

class lala:

    def __init__(self):
        self._ch = Channel(host="localhost", port=8080)
        self._stb = DeltaServiceStub(self._ch)
        pass

    def handshake(self):
        async def handshake_async():
            response = await self._stb.handshake(HandshakeRequest())
            return response
        return asyncio.run(handshake_async())

    def close(self):
        self._ch.close()


if __name__ == '__main__':
    # if sys.version_info < (3, 10):
    #     loop = asyncio.get_event_loop()
    # else:
    #     try:
    #         loop = asyncio.get_running_loop()
    #     except RuntimeError:
    #         loop = asyncio.new_event_loop()
    # asyncio.set_event_loop(loop)
    #
    #
    # #ch = Channel(host="localhost", port=8080)
    # l = lala()
    # print(l.handshake())
    # l.close()

    ch = Channel(host="localhost", port=8080)
    client = create_client(ch)
    print(client.handshake())
    print(client.list_schemas())
    print(client.get_schema("metadata"))
    print(client.get_schema("bobob"))

    prepr = client.prepare_sql_statement("SELECT * FROM `metadata`.`TABLES`")
    print(prepr)
    er = client.execute(prepr.statement)
    print(er)

    client.close()