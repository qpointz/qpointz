import os
import unittest

from millclient import *
from millclient.proto.io.qpointz.mill import ProtocolVersion

class ClientBaseTests:

    class MillClientTest(unittest.TestCase):

        def skip_all(self):
            print(f"Skipping tests. Active profile: {self.__active_profile}. Test profile: {self.__test_profile}")

        def __init__(self, methodName, valid_sql, schema_name, empty_query_predicate = None, test_profile:str = "all"):
            self.__valid_sql = valid_sql
            self.__schema_name = schema_name
            self.__empty_query_predicate = empty_query_predicate or " `ID` < 0 "

            self.__test_profile = test_profile

            profile = os.environ.get("MILL_TEST_PROFILE", "all")
            self.__active_profile = profile
            if (profile == "all" or profile == self.__test_profile):
                super().__init__(methodName)
            else:
                self.skip_all()
                super().__init__('skip_all')

        @abstractmethod
        def __client__(self):
            pass

        @abstractmethod
        def __get_schema_name__(self):
            pass


        def test_handshake(self):
            with self.__client__() as c:
                r = c.handshake()
                assert r.version == ProtocolVersion.V1_0

        def test_list_schemas(self):
            with self.__client__() as c:
                r = c.list_schemas()
                assert len(r.schemas) > 0
                print(r.schemas)

        def test_get_schema(self):
            with self.__client__() as c:
                r = c.get_schema(schema_name=self.__schema_name)
                assert len(r.schema.tables) > 0

        def test_schema_doesnt_exist(self):
            with self.__client__() as c:
                self.assertRaises(MillServerError, c.get_schema, schema_name="airlines-not-exists")


        def test_get_scema_pass_request(self):
            with self.__client__() as c:
                req = GetSchemaRequest(schema_name=self.__schema_name)
                r = c.get_schema(request=req)
                assert len(r.schema.tables) > 0

        def test_exec_query(self):
            with self.__client__() as c:
                l = c.exec_query_fetch(sql = self.__valid_sql, fetch_size = 10)

        def test_sql_querty_trivial(self):
            with self.__client__() as c:
                q = c.sql_query(sql = self.__valid_sql, fetch_size = 10)
                l = q.responses_fetch()
                assert len(l) > 0

        def trivia(self, c: MillClient):
            return c.sql_query(sql = self.__valid_sql, fetch_size = 10)

        def test_query_record_batches(self):
            with self.__client__() as c:
                q = self.trivia(c)
                l = q.record_batches_fetch()
                assert len(l) > 0

        def test_query_to_pandas(self):
            with self.__client__() as c:
                q = c.sql_query(sql = self.__valid_sql)
                df = q.to_pandas()
                assert len(df) > 0

        def test_empty_query_returns_schema(self):
            with self.__client__() as c:
                query = self.__valid_sql + " WHERE " + self.__empty_query_predicate or " 1 = 2 "
                q = c.sql_query(sql = query, fetch_size = 10)
                df = q.to_pandas()
                print(df)
                assert len(df) == 0
                assert len(df.columns)>0


def run_test_profile(name):
    profile = os.environ.get("MILL_TEST_PROFILE", "all")
    print(f"Profiles: Requested:{name}. Selected:{profile}")
    if (profile == "all" or profile == name):
        print(f"Executing tests in profile {name}")
        asyncio.set_event_loop(asyncio.new_event_loop())
        unittest.main()
    else:
        print(f"Skipping tests in profile {name}")
