import io
import unittest

import requests

from millclient import *


class CloudFuncClientTests:

    def test_trivial_list_schemas(self):
        req = ListSchemasRequest()
        res = requests.post(url='http://localhost:7072/api/listSchemas',
                            data = req.to_json()
                            , headers={'Content-Type': 'application/json'}
                            )
        cnt = res.content.__bytes__()
        resPB = ListSchemasResponse().parse(cnt)
        print(resPB.to_json())

    def test_trivial_getschema(self):
        req = GetSchemaRequest()
        req.schema_name = "ts"
        res = requests.post(url='http://localhost:7072/api/getSchema',
                            data = req.to_json(),
                            headers={
                                'Content-Type': 'application/json',
                                'Accept': 'application/protobuf'
                            })
        cnt = GetSchemaResponse().parse(res.content.__bytes__())
        print(cnt)
