import os
from millclient import *
from tests.clients_base_test import ClientBaseTests, run_test_profile


class MillHttpClientTests(ClientBaseTests.MillClientTest):

    def __init__(self, methodName):
        super().__init__(methodName,"select * from `ts`.`TEST`", "ts", " `ID` < 0 ", "http")

    def __client__(self):
        url = os.environ.get("MILL_AZ_FUNC_API_URL", "http://localhost:7071/api/")
        return create_client(url = url)

if __name__ == '__main__':
    run_test_profile("http")