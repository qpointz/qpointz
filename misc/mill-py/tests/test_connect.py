# import asyncio
# import os
# import ssl
# import unittest
#
# from millclient import *
#
# class MillConnectTests(unittest.TestCase):
#
#     def mill_host_params(self):
#         host = os.environ.get("MILL_AUTH_TLS_HOST", "backend.local")
#         port = int(os.environ.get("MILL_PORT", "9090"))
#         ca_file = os.environ.get("TLS_ROOT_CA", '../../../etc/ssl/ca.pem')
#         ctx = ssl.create_default_context(ssl.Purpose.SERVER_AUTH, cafile=ca_file)
#         ctx.set_alpn_protocols(['h2'])
#         ctx.check_hostname = False #test purpose for wildcard certificates
#         print(f"Connect to {host}:{port}")
#         return host, port, ctx
#
#     def test_connect_secure(self):
#         host, port, ssl_context = self.mill_host_params()
#         all_creds = [
#             BasicAuthCredentials("reader", "reader"),
#         ]
#         az_test_token=os.environ.get('MILL_JWT_TOKEN', None)
#         if az_test_token or os.environ.get('CI','false') == 'true':
#             print("AZ token provided OR CI mode")
#             all_creds.append(BearerTokenCredentials(az_test_token))
#         else:
#             print("No AZ token provided skipping")
#
#         for creds in all_creds:
#             with self.subTest(msg=f"{creds}"):
#                 with create_client(host = host, port=port, ssl=ssl_context, creds=creds) as c:
#                     r = c.handshake()
#                     print(f"Logged in as {r.authentication.name}")
#                     self.assertNotEqual(r.authentication.name, "ANONYMOUS")
#
# if __name__ == '__main__':
#     asyncio.set_event_loop(asyncio.new_event_loop())
#     unittest.main()
#
