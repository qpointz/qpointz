"""Integration tests — authentication behaviour.

These tests are skipped when ``MILL_IT_AUTH=none`` (the default).
They verify that the authenticated identity is non-anonymous and that
bad credentials are rejected.
"""
from __future__ import annotations

import pytest

import mill
from mill.auth import BasicAuth, BearerToken
from mill.exceptions import MillAuthError
from .conftest import IntegrationConfig


def _requires_auth(mill_config: IntegrationConfig) -> None:
    """Skip the test if auth is not configured."""
    if mill_config.auth_mode == "none":
        pytest.skip("Auth tests skipped: MILL_IT_AUTH=none")


@pytest.mark.integration
class TestAuthenticatedIdentity:
    """Verify the server reports a non-anonymous identity when auth is set."""

    def test_handshake_returns_named_user(
        self, mill_client: mill.MillClient, mill_config: IntegrationConfig,
    ) -> None:
        _requires_auth(mill_config)
        resp = mill_client.handshake()
        assert resp.HasField("authentication"), "No authentication context"
        name = resp.authentication.name
        assert name, "Authentication name is empty"
        assert name.upper() != "ANONYMOUS", (
            f"Expected non-anonymous identity, got {name!r}"
        )


@pytest.mark.integration
class TestBasicAuthRejection:
    """Verify bad basic-auth credentials are rejected."""

    def test_wrong_password_raises(
        self, mill_config: IntegrationConfig,
    ) -> None:
        if mill_config.auth_mode != "basic":
            pytest.skip("Basic-auth rejection test requires MILL_IT_AUTH=basic")

        bad_auth = BasicAuth(mill_config.username, "WRONG_PASSWORD_XYZ")
        with pytest.raises((MillAuthError, Exception)):
            client = mill.connect(
                mill_config.url,
                auth=bad_auth,
                encoding=mill_config.encoding,
            )
            try:
                client.handshake()
            finally:
                client.close()

    def test_wrong_username_raises(
        self, mill_config: IntegrationConfig,
    ) -> None:
        if mill_config.auth_mode != "basic":
            pytest.skip("Basic-auth rejection test requires MILL_IT_AUTH=basic")

        bad_auth = BasicAuth("NONEXISTENT_USER_XYZ", "bad")
        with pytest.raises((MillAuthError, Exception)):
            client = mill.connect(
                mill_config.url,
                auth=bad_auth,
                encoding=mill_config.encoding,
            )
            try:
                client.handshake()
            finally:
                client.close()


@pytest.mark.integration
class TestBearerTokenRejection:
    """Verify an invalid bearer token is rejected."""

    def test_invalid_token_raises(
        self, mill_config: IntegrationConfig,
    ) -> None:
        if mill_config.auth_mode != "bearer":
            pytest.skip("Bearer rejection test requires MILL_IT_AUTH=bearer")

        bad_auth = BearerToken("INVALID_TOKEN_XYZ_000")
        with pytest.raises((MillAuthError, Exception)):
            client = mill.connect(
                mill_config.url,
                auth=bad_auth,
                encoding=mill_config.encoding,
            )
            try:
                client.handshake()
            finally:
                client.close()
