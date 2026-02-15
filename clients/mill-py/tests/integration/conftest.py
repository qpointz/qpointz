"""Integration test conftest — configuration, fixtures, and skip logic.

All configuration is driven by environment variables prefixed with ``MILL_IT_``.
When no variables are set the suite defaults to **gRPC on localhost:9099,
no TLS, no auth** so that a bare ``pytest -m integration`` works against a
local dev server out of the box.

Environment variables
---------------------
MILL_IT_HOST        Server hostname (default: localhost)
MILL_IT_PORT        Server port (default: protocol-dependent — 9099 for gRPC, 8501 for HTTP)
MILL_IT_PROTOCOL    One of: grpc, http-json, http-protobuf (default: grpc)
MILL_IT_BASE_PATH   HTTP base path prefix (default: /services/jet; ignored for gRPC)
MILL_IT_TLS         true / false (default: false)
MILL_IT_TLS_CA      Path to PEM CA certificate file for server verification
MILL_IT_TLS_CERT    Path to PEM client certificate file (mutual-TLS)
MILL_IT_TLS_KEY     Path to PEM client private-key file (mutual-TLS)
MILL_IT_AUTH        One of: none, basic, bearer (default: none)
MILL_IT_USERNAME    Basic-auth username (default: reader)
MILL_IT_PASSWORD    Basic-auth password (default: reader)
MILL_IT_TOKEN       Bearer token string
MILL_IT_SCHEMA      Schema name to test against (default: skymill)
"""
from __future__ import annotations

import os
from dataclasses import dataclass

import pytest

import mill
from mill.auth import BasicAuth, BearerToken, Credential


# ---------------------------------------------------------------------------
# Configuration dataclass
# ---------------------------------------------------------------------------

@dataclass(frozen=True, slots=True)
class IntegrationConfig:
    """Parsed integration-test configuration from environment variables."""

    host: str
    port: int
    protocol: str          # "grpc" | "http-json" | "http-protobuf"
    base_path: str         # HTTP base path prefix (ignored for gRPC)
    tls: bool
    tls_ca: str | None     # path to CA PEM file
    tls_cert: str | None   # path to client cert PEM file
    tls_key: str | None    # path to client key PEM file
    auth_mode: str         # "none" | "basic" | "bearer"
    username: str
    password: str
    token: str
    schema_name: str

    @property
    def credential(self) -> Credential:
        """Build the appropriate credential object."""
        if self.auth_mode == "basic":
            return BasicAuth(self.username, self.password)
        if self.auth_mode == "bearer":
            return BearerToken(self.token)
        return None

    @property
    def url(self) -> str:
        """Build the connect() URL from protocol, host, port, and TLS."""
        if self.protocol == "grpc":
            scheme = "grpcs" if self.tls else "grpc"
            return f"{scheme}://{self.host}:{self.port}"
        # http-json or http-protobuf
        scheme = "https" if self.tls else "http"
        return f"{scheme}://{self.host}:{self.port}"

    @property
    def encoding(self) -> str:
        """HTTP encoding (ignored for gRPC)."""
        if self.protocol == "http-protobuf":
            return "protobuf"
        return "json"


def _default_port(protocol: str, tls: bool) -> int:
    """Return the default port for the given protocol."""
    if protocol == "grpc":
        return 443 if tls else 9099
    return 443 if tls else 8501


def _env_path(var: str) -> str | None:
    """Read an env var as a file path, returning None if empty or unset."""
    val = os.environ.get(var, "").strip()
    return val if val else None


def _read_config() -> IntegrationConfig:
    """Read configuration from environment.

    Defaults to ``grpc://localhost:9099``, no TLS, no auth when nothing is set.
    """
    protocol = os.environ.get("MILL_IT_PROTOCOL", "grpc").strip().lower()
    tls = os.environ.get("MILL_IT_TLS", "false").strip().lower() == "true"
    host = os.environ.get("MILL_IT_HOST", "localhost").strip()
    port_str = os.environ.get("MILL_IT_PORT", "").strip()
    port = int(port_str) if port_str else _default_port(protocol, tls)
    base_path = os.environ.get("MILL_IT_BASE_PATH", "/services/jet").strip()

    tls_ca = _env_path("MILL_IT_TLS_CA")
    tls_cert = _env_path("MILL_IT_TLS_CERT")
    tls_key = _env_path("MILL_IT_TLS_KEY")

    auth_mode = os.environ.get("MILL_IT_AUTH", "none").strip().lower()
    username = os.environ.get("MILL_IT_USERNAME", "reader").strip()
    password = os.environ.get("MILL_IT_PASSWORD", "reader").strip()
    token = os.environ.get("MILL_IT_TOKEN", "").strip()
    schema_name = os.environ.get("MILL_IT_SCHEMA", "skymill").strip()

    return IntegrationConfig(
        host=host,
        port=port,
        protocol=protocol,
        base_path=base_path,
        tls=tls,
        tls_ca=tls_ca,
        tls_cert=tls_cert,
        tls_key=tls_key,
        auth_mode=auth_mode,
        username=username,
        password=password,
        token=token,
        schema_name=schema_name,
    )


# ---------------------------------------------------------------------------
# Fixtures
# ---------------------------------------------------------------------------

@pytest.fixture(scope="session")
def mill_config() -> IntegrationConfig:
    """Session-scoped configuration parsed from MILL_IT_* env vars.

    Defaults to ``grpc://localhost:9099``, no TLS, no auth.
    """
    return _read_config()


@pytest.fixture(scope="session")
def mill_client(mill_config: IntegrationConfig) -> mill.MillClient:
    """Session-scoped Mill client connected per the integration config.

    Automatically closed after the test session.
    """
    client = mill.connect(
        mill_config.url,
        auth=mill_config.credential,
        encoding=mill_config.encoding,
        base_path=mill_config.base_path,
        tls_ca=mill_config.tls_ca,
        tls_cert=mill_config.tls_cert,
        tls_key=mill_config.tls_key,
    )
    yield client  # type: ignore[misc]
    client.close()


@pytest.fixture(scope="session")
def schema_name(mill_config: IntegrationConfig) -> str:
    """The schema name to test against (from MILL_IT_SCHEMA)."""
    return mill_config.schema_name
