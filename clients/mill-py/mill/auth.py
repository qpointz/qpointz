"""Authentication credentials for Mill transports.

Three credential modes are supported, matching the server's
``AuthenticationType`` enum:

- **BasicAuth** — HTTP Basic (base64 ``user:password``).
- **BearerToken** — OAuth2 / JWT bearer token.
- **Anonymous** — No auth header (the default when ``auth=None``).

All credential classes expose :meth:`to_headers` which returns a dict
suitable for injection into transport metadata (gRPC) or HTTP headers.
The transport layer handles key-casing differences internally.
"""
from __future__ import annotations

import base64
from dataclasses import dataclass


@dataclass(frozen=True, slots=True)
class BasicAuth:
    """HTTP Basic authentication.

    Args:
        username: The user name.
        password: The password.

    Example::

        >>> auth = BasicAuth("reader", "secret")
        >>> auth.to_headers()
        {'authorization': 'Basic cmVhZGVyOnNlY3JldA=='}
    """

    username: str
    password: str

    def to_headers(self) -> dict[str, str]:
        """Return the ``authorization`` header for this credential.

        Returns:
            A single-entry dict with lowercase ``authorization`` key.
        """
        token = base64.b64encode(
            f"{self.username}:{self.password}".encode()
        ).decode("ascii")
        return {"authorization": f"Basic {token}"}


@dataclass(frozen=True, slots=True)
class BearerToken:
    """OAuth2 / JWT bearer-token authentication.

    Args:
        token: The bearer token string.

    Example::

        >>> auth = BearerToken("eyJhbGci...")
        >>> auth.to_headers()
        {'authorization': 'Bearer eyJhbGci...'}
    """

    token: str

    def to_headers(self) -> dict[str, str]:
        """Return the ``authorization`` header for this credential.

        Returns:
            A single-entry dict with lowercase ``authorization`` key.
        """
        return {"authorization": f"Bearer {self.token}"}


# Type alias for any accepted credential type (including None = anonymous)
Credential = BasicAuth | BearerToken | None


def _auth_headers(auth: Credential) -> dict[str, str]:
    """Resolve a credential to its header dict (empty for anonymous).

    Args:
        auth: A credential instance, or ``None`` for anonymous.

    Returns:
        Header dict — may be empty.
    """
    if auth is None:
        return {}
    return auth.to_headers()
