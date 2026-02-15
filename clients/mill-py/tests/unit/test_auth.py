"""Tests for mill.auth — credential classes and header generation."""
from __future__ import annotations

import base64

import pytest

from mill.auth import BasicAuth, BearerToken, _auth_headers


class TestBasicAuth:
    def test_to_headers_format(self) -> None:
        auth = BasicAuth("reader", "secret")
        headers = auth.to_headers()
        assert "authorization" in headers
        assert headers["authorization"].startswith("Basic ")

    def test_to_headers_base64(self) -> None:
        auth = BasicAuth("reader", "secret")
        token = auth.to_headers()["authorization"].split(" ", 1)[1]
        decoded = base64.b64decode(token).decode()
        assert decoded == "reader:secret"

    def test_empty_password(self) -> None:
        auth = BasicAuth("user", "")
        token = auth.to_headers()["authorization"].split(" ", 1)[1]
        decoded = base64.b64decode(token).decode()
        assert decoded == "user:"

    def test_special_chars_in_password(self) -> None:
        auth = BasicAuth("user", "p@ss:w0rd!")
        token = auth.to_headers()["authorization"].split(" ", 1)[1]
        decoded = base64.b64decode(token).decode()
        assert decoded == "user:p@ss:w0rd!"

    def test_frozen(self) -> None:
        auth = BasicAuth("u", "p")
        with pytest.raises(AttributeError):
            auth.username = "x"  # type: ignore[misc]


class TestBearerToken:
    def test_to_headers_format(self) -> None:
        auth = BearerToken("eyJhbGciOiJIUzI1NiJ9")
        headers = auth.to_headers()
        assert headers == {"authorization": "Bearer eyJhbGciOiJIUzI1NiJ9"}

    def test_frozen(self) -> None:
        auth = BearerToken("tok")
        with pytest.raises(AttributeError):
            auth.token = "x"  # type: ignore[misc]


class TestAuthHeaders:
    def test_none_returns_empty(self) -> None:
        assert _auth_headers(None) == {}

    def test_basic_auth(self) -> None:
        headers = _auth_headers(BasicAuth("u", "p"))
        assert "authorization" in headers
        assert headers["authorization"].startswith("Basic ")

    def test_bearer_token(self) -> None:
        headers = _auth_headers(BearerToken("tok"))
        assert headers == {"authorization": "Bearer tok"}
