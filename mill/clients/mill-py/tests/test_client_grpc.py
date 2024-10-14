import os
import ssl
from millclient import *
from tests.clients_base_test import ClientBaseTests, run_test_profile


class MillGrpcClientTests(ClientBaseTests.MillClientTest):

    def __init__(self, methodName):
        #super().__init__(methodName, "select * from `airlines`.`segments`", "airlines", " 1 = 2 ", "grpc")
        super().__init__(methodName, "select * from `ts`.`TEST`", "ts", " `ID` < 0 ", "grpc")

    def __client__(self):
        host = os.environ.get("MILL_AUTH_TLS_HOST", "mill.local")
        port = os.environ.get("MILL_PORT", "9099")
        ctx = False
        if host != "mill.local":
            ca_file = os.environ.get("TLS_ROOT_CA", '../../../etc/ssl/ca.pem')
            print(f"use CA:{ca_file}:exists{os.path.isfile(ca_file)}")
            ctx = ssl.create_default_context(ssl.Purpose.SERVER_AUTH, cafile=ca_file)
            ctx.set_alpn_protocols(['h2'])
            ctx.check_hostname = False
        else:
            host = os.environ.get("MILL_HOST", "localhost")


        print(f"Connect to {host}:{port}")
        return create_client(protocol="grpc", host=host, port=int(port), ssl=ctx, creds=BasicAuthCredentials("reader", "reader"))

if __name__ == '__main__':
    run_test_profile("grpc")