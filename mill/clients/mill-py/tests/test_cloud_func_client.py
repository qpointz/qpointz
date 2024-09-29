import unittest

import requests

from millclient import HandshakeRequest


class CloudFuncClientTests(unittest.TestCase):

    def test_trivial_handshake(self):
        req = HandshakeRequest()
        a = req.__bytes__()
        res = requests.post(url='http://localhost:7072/api/handshake',
                            data=a,
                            headers={'Content-Type': 'application/octet-stream'})




if __name__ == '__main__':
    unittest.main()