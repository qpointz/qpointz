"""Capability-gated SQL helpers for ibis + Mill."""
from __future__ import annotations

import re
from dataclasses import dataclass

from mill.sql import MillDialectDescriptor


class MillIbisCapabilityError(NotImplementedError):
    """Raised when an expression needs a disabled dialect capability."""


@dataclass(slots=True, frozen=True)
class MillIbisCompiler:
    """Validate compiled SQL against advertised dialect capabilities."""

    descriptor: MillDialectDescriptor

    def _supports(self, *flag_aliases: str) -> bool:
        for alias in flag_aliases:
            if alias in self.descriptor.feature_flags:
                return bool(self.descriptor.feature_flags[alias])
        return False

    def validate_sql(self, sql: str) -> None:
        """Raise explicit errors for unsupported SQL features."""
        normalized = sql.upper()

        if re.search(r"\bWITH\b", normalized) and not self._supports(
            "supports-with",
            "supportsWith",
            "supports-cte",
            "supportsCte",
        ):
            raise MillIbisCapabilityError(
                "CTE (WITH) is not supported by the active Mill dialect."
            )

        if re.search(r"\bINTERSECT\b", normalized) and not self._supports(
            "supports-intersect",
            "supportsIntersect",
        ):
            raise MillIbisCapabilityError(
                "INTERSECT is not supported by the active Mill dialect."
            )

        if re.search(r"\bEXCEPT\b", normalized) and not self._supports(
            "supports-except",
            "supportsExcept",
        ):
            raise MillIbisCapabilityError(
                "EXCEPT is not supported by the active Mill dialect."
            )

        if re.search(r"\bLATERAL\b", normalized) and not self._supports(
            "supports-lateral",
            "supportsLateral",
        ):
            raise MillIbisCapabilityError(
                "LATERAL joins are not supported by the active Mill dialect."
            )
