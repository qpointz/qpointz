import unittest
from unittest import TestCase
import pytest

from millclient import *
from millclient.proto.io.qpointz.mill import ProtocolVersion
from tests.integration.profiles import TestITProfile, profiles, Authentication, Protocol


# def __init__(self):
    # print(f"Running test for: {test_profile}")
    # self.__valid_sql = "select `CLIENT_ID`, `FIRST_NAME`, `LAST_NAME` from `MONETA`.`CLIENTS`"
    # self.__schema_name = "MONETA"
    # self.__empty_query_predicate = empty_query_predicate or " `CLIENT_ID` < 0 "
    # (methodName,)
    # (valid_sql,)
    # (schema_name,)
    # empty_query_predicate = (None,)
    # test_profile: str = ("all",)

    # self.__test_profile = test_profile

    # profile = os.environ.get("MILL_TEST_PROFILE", "all")
    # self.__active_profile = profile
    # if (profile == "all" or profile == self.__test_profile):
    #     super().__init__(methodName)
    # else:
    #     self.skip_all()
    #     super().__init__('skip_all')

def client(profile: TestITProfile):
    ssl = False

    if profile.tls:
        raise Exception("TLS not supported yet.")

    if profile.auth != Authentication.NO_AUTH:
        raise Exception("Auth not supported yet.")

    if profile.protocol == Protocol.HTTP:
        protocol = "http"
        return create_client(
            protocol=protocol,
            host=profile.host,
            port=profile.port,
            base_path="/services/jet",
            ssl=ssl,
        )

    if profile.protocol == Protocol.GRPC:
        return create_client(
            protocol="grpc", host=profile.host, port=profile.port, ssl=ssl
        )

    raise Exception("Unknown protocol")
    pass


@pytest.mark.parametrize("profile", profiles(),  ids=lambda p: f"{p}" )
def test_handshake(profile: TestITProfile):
    with client(profile) as c:
        r = c.handshake()
        assert r.version == ProtocolVersion.V1_0

@pytest.mark.parametrize("profile", profiles(),  ids=lambda p: f"{p}" )
def test_list_schemas(profile: TestITProfile):
    with client(profile) as c:
        r = c.list_schemas()
        assert len(r.schemas) > 0
        print(r.schemas)

@pytest.mark.parametrize("profile", profiles(),  ids=lambda p: f"{p}" )
def test_get_schema(profile: TestITProfile):
    with client(profile) as c:
        r = c.get_schema(schema_name="MONETA")
        assert len(r.schema.tables) > 0

@pytest.mark.parametrize("profile", profiles(),  ids=lambda p: f"{p}" )
def test_schema_doesnt_exist(profile: TestITProfile):
    with client(profile) as c:
        with pytest.raises(MillServerError):
            c.get_schema(schema_name="NO-SHCEMA")

@pytest.mark.parametrize("profile", profiles(),  ids=lambda p: f"{p}" )
def test_get_scema_pass_request(profile: TestITProfile):
    with client(profile) as c:
        req = GetSchemaRequest(schema_name="MONETA")
        r = c.get_schema(request=req)
        assert len(r.schema.tables) > 0

@pytest.mark.parametrize("profile", profiles(),  ids=lambda p: f"{p}" )
def test_exec_query(profile: TestITProfile):
    with client(profile) as c:
        sql = "select `CLIENT_ID`, `FIRST_NAME`, `LAST_NAME` from `MONETA`.`CLIENTS`"
        l = c.exec_query_fetch(sql=sql, fetch_size=10)

@pytest.mark.parametrize("profile", profiles(),  ids=lambda p: f"{p}" )
def test_sql_querty_trivial(profile: TestITProfile):
    with client(profile) as c:
        sql = "select `CLIENT_ID`, `FIRST_NAME`, `LAST_NAME` from `MONETA`.`CLIENTS`"
        q = c.sql_query(sql=sql, fetch_size=10)
        l = q.responses_fetch()
        assert len(l) > 0


@pytest.mark.parametrize("profile", profiles(),  ids=lambda p: f"{p}" )
def test_query_record_batches(profile: TestITProfile):
    with client(profile) as c:
        sql = "select `CLIENT_ID`, `FIRST_NAME`, `LAST_NAME` from `MONETA`.`CLIENTS`"
        q = c.sql_query(sql = sql, fetch_size = 1)
        l = q.record_batches_fetch()
        assert len(l) > 0

@pytest.mark.parametrize("profile", profiles(),  ids=lambda p: f"{p}" )
def test_query_to_pandas(profile: TestITProfile):
    with client(profile) as c:
        sql = "select `CLIENT_ID`, `FIRST_NAME`, `LAST_NAME` from `MONETA`.`CLIENTS`"
        q = c.sql_query(sql=sql)
        df = q.to_pandas()
        assert len(df) > 0

@pytest.mark.parametrize("profile", profiles(),  ids=lambda p: f"{p}" )
def test_empty_query_returns_schema(profile: TestITProfile):
    with client(profile) as c:
        sql = "select `CLIENT_ID`, `FIRST_NAME`, `LAST_NAME` from `MONETA`.`CLIENTS` WHERE `CLIENT_ID` < 0"
        q = c.sql_query(sql=sql, fetch_size=10)
        df = q.to_pandas()
        print(df)
        assert len(df) == 0
        assert len(df.columns) > 0

if __name__ == '__main__':
    unittest.main()
