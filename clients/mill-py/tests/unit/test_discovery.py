"""Tests for mill.discovery — descriptor parsing and fetch stub."""
from __future__ import annotations

import pytest

from mill.discovery import (
    MillSchemaLink,
    MillSecurityDescriptor,
    MillServiceDescriptor,
    MillServiceEndpoint,
    _parse_descriptor,
)


class TestParseDescriptor:
    def test_empty_document(self) -> None:
        desc = _parse_descriptor({})
        assert desc.services == ()
        assert desc.security.enabled is False
        assert desc.schemas == {}

    def test_services(self) -> None:
        data = {
            "services": [
                {"stereotype": "grpc", "url": "grpc://host:9090"},
                {"stereotype": "http-json", "url": "http://host:8501/services/jet"},
            ],
        }
        desc = _parse_descriptor(data)
        assert len(desc.services) == 2
        assert desc.services[0].stereotype == "grpc"
        assert desc.services[1].url == "http://host:8501/services/jet"

    def test_security(self) -> None:
        data = {
            "security": {
                "enabled": True,
                "authMethods": ["BASIC", "OAUTH2"],
            },
        }
        desc = _parse_descriptor(data)
        assert desc.security.enabled is True
        assert desc.security.auth_methods == ("BASIC", "OAUTH2")

    def test_schemas(self) -> None:
        data = {
            "schemas": {
                "MONETA": {"name": "MONETA", "link": "/schemas/MONETA"},
                "TEST": {"name": "TEST", "link": "/schemas/TEST"},
            },
        }
        desc = _parse_descriptor(data)
        assert len(desc.schemas) == 2
        assert desc.schemas["MONETA"].link == "/schemas/MONETA"

    def test_full_document(self) -> None:
        data = {
            "services": [{"stereotype": "grpc", "url": "grpc://h:9090"}],
            "security": {"enabled": True, "authMethods": ["BASIC"]},
            "schemas": {"S": {"name": "S", "link": "/s"}},
        }
        desc = _parse_descriptor(data)
        assert len(desc.services) == 1
        assert desc.security.enabled is True
        assert "S" in desc.schemas


class TestDataclasses:
    def test_endpoint_frozen(self) -> None:
        ep = MillServiceEndpoint(stereotype="grpc", url="grpc://h:9090")
        with pytest.raises(AttributeError):
            ep.stereotype = "http"  # type: ignore[misc]

    def test_security_frozen(self) -> None:
        sec = MillSecurityDescriptor(enabled=True, auth_methods=("BASIC",))
        with pytest.raises(AttributeError):
            sec.enabled = False  # type: ignore[misc]

    def test_schema_link_frozen(self) -> None:
        sl = MillSchemaLink(name="X", link="/x")
        with pytest.raises(AttributeError):
            sl.name = "Y"  # type: ignore[misc]

    def test_descriptor_frozen(self) -> None:
        desc = MillServiceDescriptor()
        with pytest.raises(AttributeError):
            desc.services = ()  # type: ignore[misc]
