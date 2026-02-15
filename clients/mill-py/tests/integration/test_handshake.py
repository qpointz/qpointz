"""Integration tests — protocol handshake."""
from __future__ import annotations

import pytest

from mill import MillClient
from mill._proto.common_pb2 import V1_0


@pytest.mark.integration
class TestHandshake:
    """Validate handshake response from the live service."""

    def test_protocol_version(self, mill_client: MillClient) -> None:
        resp = mill_client.handshake()
        assert resp.version == V1_0, (
            f"Expected ProtocolVersion.V1_0 ({V1_0}), got {resp.version}"
        )

    def test_capabilities_populated(self, mill_client: MillClient) -> None:
        resp = mill_client.handshake()
        assert resp.HasField("capabilities"), "Handshake response missing capabilities"
