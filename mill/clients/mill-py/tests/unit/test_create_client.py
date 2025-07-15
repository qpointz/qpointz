# import asyncio
# import unittest
#
# from millclient import create_client, MillHttpClient, MillGrpcClient, MillClient
#
#
# class MillConnectTests(unittest.TestCase):
#     def test_http_std(self):
#         c: MillHttpClient | MillGrpcClient | MillClient = create_client(url= "http://127.0.0.1:8501/grpc")
#         assert isinstance(c, MillHttpClient)
#         print(c.get_base_url())
#         assert c.get_base_url()=="http://127.0.0.1:8501/grpc/"
#
#     def test_create_grpc_client(self):
#         c: MillGrpcClient = create_client(host="HOST1", port=8080)
#         assert isinstance(c, MillGrpcClient)
#
#     def test_create_http_client_by_host(self):
#         c: MillClient = create_client(protocol="http", host="host1", port=8080)
#         assert isinstance(c, MillHttpClient)
#         print(c.get_base_url())
#         assert c.get_base_url() == "http://host1:8080/api/"
#
#     def test_create_http_client_by_host_no_port(self):
#         c: MillClient = create_client(protocol="http", host="host1")
#         assert isinstance(c, MillHttpClient)
#         print(c.get_base_url())
#         assert c.get_base_url() == "http://host1/api/"
#
#     def test_create_http_no_base_path_to_default(self):
#         c: MillClient = create_client(url="https://host1")
#         assert isinstance(c, MillHttpClient)
#         print(c.get_base_url())
#         assert c.get_base_url() == "https://host1/api/"
#
#     def test_create_http_no_base_path_as_spec(self):
#         c: MillClient = create_client(url="http://host1/")
#         assert isinstance(c, MillHttpClient)
#         print(c.get_base_url())
#         assert c.get_base_url() == "http://host1/"
#
#
# if __name__ == '__main__':
#     asyncio.set_event_loop(asyncio.new_event_loop())
#     unittest.main()