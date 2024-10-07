import os
import ssl
import unittest
from millclient import *
from millclient.proto.io.qpointz.mill import ProtocolVersion


class ClientBaseTests:

    class MillClientTest(unittest.TestCase):

        def __init__(self, methodName, valid_sql, schema_name, empty_query_predicate = None):
            super().__init__(methodName)
            self.__valid_sql = valid_sql
            self.__schema_name = schema_name
            self.__empty_query_predicate = empty_query_predicate or " `ID` < 0 "

        @abstractmethod
        def __client__(self):
            pass

        @abstractmethod
        def __get_schema_name__(self):
            pass

        def test_handshake(self):
            with self.__client__() as c:
                r = c.handshake()
                assert r.version == ProtocolVersion.V1_0

        def test_list_schemas(self):
            with self.__client__() as c:
                r = c.list_schemas()
                assert len(r.schemas) > 0
                print(r.schemas)

        def test_get_schema(self):
            with self.__client__() as c:
                r = c.get_schema(schema_name=self.__schema_name)
                assert len(r.schema.tables) > 0

        def test_schema_doesnt_exist(self):
            with self.__client__() as c:
                self.assertRaises(MillServerError, c.get_schema, schema_name="airlines-not-exists")


        def test_get_scema_pass_request(self):
            with self.__client__() as c:
                req = GetSchemaRequest(schema_name=self.__schema_name)
                r = c.get_schema(request=req)
                assert len(r.schema.tables) > 0

        def test_exec_query(self):
            with self.__client__() as c:
                l = c.exec_query_fetch(sql = self.__valid_sql, fetch_size = 10)

        def test_sql_querty_trivial(self):
            with self.__client__() as c:
                q = c.sql_query(sql = self.__valid_sql, fetch_size = 10)
                l = q.responses_fetch()
                assert len(l) > 0

        def trivia(self, c: MillClient):
            return c.sql_query(sql = self.__valid_sql, fetch_size = 10)

        def test_query_record_batches(self):
            with self.__client__() as c:
                q = self.trivia(c)
                l = q.record_batches_fetch()
                assert len(l) > 0

        def test_query_to_pandas(self):
            with self.__client__() as c:
                q = c.sql_query(sql = self.__valid_sql)
                df = q.to_pandas()
                assert len(df) > 0

        def test_empty_query_returns_schema(self):
            with self.__client__() as c:
                query = self.__valid_sql + " WHERE " + self.__empty_query_predicate or " 1 = 2 "
                q = c.sql_query(sql = query, fetch_size = 10)
                df = q.to_pandas()
                print(df)
                assert len(df) == 0
                assert len(df.columns)>0

class MillGrpcClientTests(ClientBaseTests.MillClientTest):

    def __init__(self, methodName):
        super().__init__(methodName, "select * from `airlines`.`segments`", "airlines", " 1 = 2 ")


    def __client__(self):
        host = os.environ.get("MILL_AUTH_TLS_HOST", "mill.local")
        ctx = False
        if host != "mill.local":
            port = int(os.environ.get("MILL_AUTH_TLS_PORT", "9099"))
            ca_file = os.environ.get("TLS_ROOT_CA", '../../../etc/ssl/ca.pem')
            print(f"use CA:{ca_file}:exists{os.path.isfile(ca_file)}")
            ctx = ssl.create_default_context(ssl.Purpose.SERVER_AUTH, cafile=ca_file)
            ctx.set_alpn_protocols(['h2'])
            ctx.check_hostname = False
        else:
            host = os.environ.get("MILL_HOST", "localhost")
            port = os.environ.get("MILL_PORT", "9099")

        print(f"Connect to {host}:{port}")
        return create_client(host=host, port=int(port), ssl=ctx, creds=BasicAuthCredentials("reader", "reader"))

class MillHttpClientTests(ClientBaseTests.MillClientTest):

    def __init__(self, methodName):
        super().__init__(methodName,"select * from `ts`.`TEST`", "ts", " `ID` < 0 ")

    def __client__(self):
        host = os.environ.get("MILL_AZ_FUNC_HOST", "localhost")
        port = os.environ.get("MILL_AZ_FUNC_PORT", "7071")
        return create_client(url=f"http://{host}:{port}/api/")

if __name__ == '__main__':
    asyncio.set_event_loop(asyncio.new_event_loop())
    unittest.main()
