"""Phase 1 scaffold smoke tests — verify the package is importable."""
from __future__ import annotations

import pytest


@pytest.mark.unit
class TestPackageImportable:
    """Verify that the mill package and its sub-modules can be imported."""

    def test_import_mill(self) -> None:
        import mill  # noqa: F401

    def test_import_mill_types(self) -> None:
        import mill.types  # noqa: F401

    def test_import_mill_vectors(self) -> None:
        import mill.vectors  # noqa: F401

    def test_import_mill_client(self) -> None:
        import mill.client  # noqa: F401

    def test_import_mill_result(self) -> None:
        import mill.result  # noqa: F401

    def test_import_mill_auth(self) -> None:
        import mill.auth  # noqa: F401

    def test_import_mill_exceptions(self) -> None:
        import mill.exceptions  # noqa: F401

    def test_import_mill_discovery(self) -> None:
        import mill.discovery  # noqa: F401

    def test_import_mill_transport_grpc(self) -> None:
        import mill._transport._grpc  # noqa: F401

    def test_import_mill_transport_http(self) -> None:
        import mill._transport._http  # noqa: F401

    def test_import_mill_aio(self) -> None:
        import mill.aio  # noqa: F401

    def test_import_mill_extras_arrow(self) -> None:
        import mill.extras.arrow  # noqa: F401

    def test_import_mill_extras_pandas(self) -> None:
        import mill.extras.pandas  # noqa: F401

    def test_import_mill_extras_polars(self) -> None:
        import mill.extras.polars  # noqa: F401


@pytest.mark.unit
class TestProtoStubsImportable:
    """Verify that generated proto stubs can be imported."""

    def test_import_common_pb2(self) -> None:
        from mill._proto import common_pb2  # noqa: F401

    def test_import_vector_pb2(self) -> None:
        from mill._proto import vector_pb2  # noqa: F401

    def test_import_statement_pb2(self) -> None:
        from mill._proto import statement_pb2  # noqa: F401

    def test_import_data_connect_svc_pb2(self) -> None:
        from mill._proto import data_connect_svc_pb2  # noqa: F401

    def test_import_grpc_stub(self) -> None:
        from mill._proto import data_connect_svc_pb2_grpc  # noqa: F401
