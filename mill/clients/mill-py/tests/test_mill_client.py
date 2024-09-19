import os
import ssl
import unittest
from millclient import *
from millclient._auth import BasicAuthCredentials


def client():    
    host = os.environ.get("MILL_HOST_SECURE", "mill.local")
    port = 0
    ctx = False
    if host != "mill.local":
        port = int(os.environ.get("MILL_PORT_SECURE", "9099"))
        ca_file = os.environ.get("MILL_CA_FILE", '../../../etc/ssl/ca.pem')
        ctx = ssl.create_default_context(ssl.Purpose.SERVER_AUTH, cafile=ca_file)
        ctx.set_alpn_protocols(['h2'])
        ctx.check_hostname = False 
    else:
        host = os.environ.get("MILL_HOST", "localhost")
        port = os.environ.get("MILL_PORT", "9099")
    
    print(f"Connect to {host}:{port}")       
    return create_client(host=host, port=int(port), ssl=ctx, creds=BasicAuthCredentials("reader", "reader"))


class MillClientTests(unittest.TestCase):

    def test_handshake(self):
        with client() as c:
            r = c.handshake()
            print(r)

    def test_list_schemas(self):
        with client() as c:
            r = c.list_schemas()
            assert len(r.schemas) > 0
            print(r.schemas)

    def test_get_schema(self):
        with client() as c:
            r = c.get_schema(schema_name="airlines")
            assert len(r.schema.tables) > 0

    def test_schema_doesnt_exist(self):
        with client() as c:
            self.assertRaises(MillServerError, c.get_schema, schema_name="airlines-not-exists")


    def test_get_scema_pass_request(self):
        with client() as c:
            req = GetSchemaRequest(schema_name="airlines")
            r = c.get_schema(request=req)
            assert len(r.schema.tables) > 0

    def test_exec_sql(self):
        with client() as c:
            l = c.exec_sql_fetch(sql = "select * from `airlines`.`segments`", batch_size = 10)

    def test_sql_querty_trivial(self):
        with client() as c:
            q = c.sql_query(sql = "select * from `airlines`.`segments`", batch_size = 10)
            l = q.responses_fetch()
            assert len(l) > 0

    def trivia(self, c: MillClient):
        return c.sql_query(sql = "select * from `airlines`.`segments`", batch_size = 10)

    def test_query_record_batches(self):
        with client() as c:
            q = self.trivia(c)
            l = q.record_batches_fetch()
            assert len(l) > 0

    def test_query_to_pandas(self):
        with client() as c:
            q = c.sql_query(sql = "select * from `airlines`.`segments`")
            df = q.to_pandas()
            assert len(df) > 0

    def test_empty_query_returns_schema(self):
        with client() as c:
            q = c.sql_query(sql = "select * from `airlines`.`segments` WHERE 1=2")
            df = q.to_pandas()
            assert len(df) == 0
            assert len(df.columns)>0


if __name__ == '__main__':
    unittest.main()
