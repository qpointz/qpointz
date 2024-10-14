import os
from millclient import *
from tests.clients_base_test import ClientBaseTests, run_test_profile


class MillHttpClientTests(ClientBaseTests.MillClientTest):

    def __init__(self, methodName):
        super().__init__(methodName,"select * from `ts`.`TEST`", "ts", " `ID` < 0 ", "http")

    def __client__(self):
        host = os.environ.get("MILL_HOST", "localhost")
        port = os.environ.get("MILL_PORT", "7071")
        #return create_client(url=f"http://{host}:{port}/api/")
        return create_client(url="https://bckfuncchangefuncjdb-app.azurewebsites.net/api/")

if __name__ == '__main__':
    run_test_profile("http")