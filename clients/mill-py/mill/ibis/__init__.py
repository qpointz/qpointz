"""ibis integration for Mill."""
from __future__ import annotations

from .backend import Backend
from .compiler import MillIbisCapabilityError

__all__ = [
    "Backend",
    "MillIbisCapabilityError",
    "connect",
]


def connect(url: str, **kwargs) -> Backend:
    """Connect ibis to Mill using gRPC or HTTP URLs."""
    return Backend().connect(url, **kwargs)
