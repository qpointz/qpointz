import os
import unittest

import grpc
import grpclib.client
import pandas as pd

from millclient import *
from millclient import _auth


def client():
    #target = "api-local.qpointz.com"
    target = "api-local.qpointz.com"
    mill_host = os.environ.get('MILL_HOST_SECURE', target)
    # mill_token = os.environ.get('MILL_TOKEN', None)
    # if mill_host is not None:
    #     grpc.ssl_channel_credentials()
    #     creds = grpc.composite_channel_credentials(None,grpc.access_token_call_credentials(mill_token))
    #     channel = grpc.secure_channel(f"http://{mill_host}:9099", creds)
    #     return create_client(channel=channel)
    mill_host = os.environ.get("MILL_HOST_INSECURE", "localhost")

    # grpclib.client.Channel()
    # call_creds = _auth.username_password_call_credentials("reader","reader")
    # chanel_creds = grpc.ssl_channel_credentials()
    # creds = grpc.composite_channel_credentials(chanel_creds, call_creds)

    # channel = grpc.secure_channel(target, creds)
    channel = Channel(host = mill_host, port = 9099, ssl=False)
    return create_client(channel=channel)


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
            q = c.sql_query(sql = "select * from `airlines`.`passenger`")
            df = q.to_pandas()
            with pd.option_context('display.max_rows', None,
                                   'display.max_columns', None,
                                   'display.precision', 3,
                                   ):
                print(df.to_string())
            assert len(df) > 0


if __name__ == '__main__':
    unittest.main()
