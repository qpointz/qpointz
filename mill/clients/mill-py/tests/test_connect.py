import os
import ssl
import unittest

from millclient import *
from millclient._auth import *


class MillConnectTests(unittest.TestCase):

    def mill_host_params(self):
        host = os.environ.get("MILL_HOST_SECURE", "mill.local")
        port = int(os.environ.get("MILL_PORT_SECURE", "9099"))
        ca_file = os.environ.get("MILL_CA_FILE", '../../../etc/ssl/ca.pem')
        ctx = ssl.create_default_context(ssl.Purpose.SERVER_AUTH, cafile=ca_file)
        ctx.set_alpn_protocols(['h2'])
        ctx.check_hostname = False #test purpose for wildcard certificates
        print(f"Connect to {host}:{port}")
        return host, port, ctx

    def test_connect_secure(self):
        host, port, ssl_context = self.mill_host_params()
        all_creds = [
            BasicAuthCredentials("reader", "reader"),      
        ]
        az_test_token=os.environ.get('AZ_TEST_TOKEN', None)
        if az_test_token:
            print("AZ token provided")
            all_creds.append(BearerTokenCredentials(az_test_token))
        else:
            print("No AZ token provided skipping")

        for creds in all_creds:
            with self.subTest(msg=f"{creds}"):
                with create_client(host = host, port=port, ssl=ssl_context, creds=creds) as c:
                    r = c.handshake()
                    print(f"Logged in as {r.authentication.name}")
                    self.assertNotEqual(r.authentication.name, "ANONYMOUS")

if __name__ == '__main__':
    unittest.main()

