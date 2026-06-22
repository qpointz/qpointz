"""Shared Mill MCP HTTP connection settings for the Skymill LangChain demo."""

from __future__ import annotations

import os
from base64 import b64encode

DEFAULT_MCP_URL = "http://localhost:8080/services/mcp"


def mcp_url() -> str:
    """Mill MCP Streamable HTTP endpoint (no trailing slash)."""
    return os.environ.get("MILL_MCP_URL", DEFAULT_MCP_URL).rstrip("/")


def mcp_http_headers() -> dict[str, str]:
    """
    Optional auth headers for ``/services/mcp`` when ``mill.security.enable=true``.

    Supports ``MILL_MCP_TOKEN`` (Bearer) or ``MILL_MCP_BASIC_USER`` + ``MILL_MCP_BASIC_PASSWORD``.
    """
    headers: dict[str, str] = {}
    token = os.environ.get("MILL_MCP_TOKEN", "").strip()
    if token:
        headers["Authorization"] = f"Bearer {token}"
        return headers

    user = os.environ.get("MILL_MCP_BASIC_USER", "").strip()
    if user:
        password = os.environ.get("MILL_MCP_BASIC_PASSWORD", "")
        creds = b64encode(f"{user}:{password}".encode()).decode("ascii")
        headers["Authorization"] = f"Basic {creds}"
    return headers


def mill_mcp_server_config() -> dict[str, object]:
    """``MultiServerMCPClient`` entry for the Mill HTTP MCP backend."""
    config: dict[str, object] = {
        "transport": "streamable_http",
        "url": mcp_url(),
    }
    headers = mcp_http_headers()
    if headers:
        config["headers"] = headers
    return config
