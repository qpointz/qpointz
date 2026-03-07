"""Dialect descriptor model and fallback defaults for Python consumers."""

from __future__ import annotations

from dataclasses import dataclass, field
from typing import Mapping

from mill._proto import dialect_pb2 as _dialect


@dataclass(frozen=True, slots=True)
class MillDialectDescriptor:
    """Normalized dialect metadata consumed by Python integrations."""

    id: str
    name: str
    read_only: bool
    paramstyle: str
    feature_flags: Mapping[str, bool] = field(default_factory=dict)
    functions: Mapping[str, tuple[str, ...]] = field(default_factory=dict)
    schema_version: str = ""
    content_hash: str = ""
    source: str = "remote"

    @classmethod
    def from_proto(cls, response: _dialect.GetDialectResponse) -> "MillDialectDescriptor":
        """Build normalized descriptor from transport response."""
        descriptor = response.dialect
        function_names = {
            category: tuple(entry.name for entry in entries.entries)
            for category, entries in descriptor.functions.items()
        }
        return cls(
            id=descriptor.id,
            name=descriptor.name,
            read_only=descriptor.readOnly,
            paramstyle=descriptor.paramstyle,
            feature_flags=dict(descriptor.featureFlags),
            functions=function_names,
            schema_version=response.schemaVersion,
            content_hash=response.contentHash,
            source="remote",
        )

    def supports(self, flag_name: str) -> bool:
        """Return capability value with conservative default."""
        return bool(self.feature_flags.get(flag_name, False))

    def function_names(self, category: str) -> tuple[str, ...]:
        """Return known function names for a category."""
        return self.functions.get(category, ())

    def has_function(self, function_name: str, category: str | None = None) -> bool:
        """Check whether the function exists in a category or globally."""
        lookup = function_name.upper()
        if category is not None:
            return lookup in {name.upper() for name in self.function_names(category)}

        for names in self.functions.values():
            if lookup in {name.upper() for name in names}:
                return True
        return False


CALCITE_DEFAULT = MillDialectDescriptor(
    id="CALCITE",
    name="Apache Calcite",
    read_only=True,
    paramstyle="qmark",
    feature_flags={},
    functions={},
    source="fallback",
)


def fallback_descriptor(dialect_id: str | None = None) -> MillDialectDescriptor:
    """Return local default descriptor for legacy servers.

    Only CALCITE is available as a built-in fallback.
    """
    resolved = (dialect_id or CALCITE_DEFAULT.id).upper()
    if resolved != CALCITE_DEFAULT.id:
        raise ValueError(
            f"No built-in fallback descriptor for dialect {resolved!r}. "
            f"Only {CALCITE_DEFAULT.id!r} is available."
        )
    return CALCITE_DEFAULT
