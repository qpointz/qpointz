"""Action-grouped report data models."""

from dataclasses import dataclass, field
from typing import List, Dict, Any


@dataclass
class VersionAction:
    """Represents an action result for a specific version."""

    version: str
    action: Dict[str, Any]  # Full result_data from ParsedResult


@dataclass
class ActionEntry:
    """Represents one action with all its versions."""

    action: Dict[str, Any]  # Contains scenario, group, key, params
    versions: List[VersionAction] = field(default_factory=list)


@dataclass
class ActionGroupedReport:
    """Container for action-grouped report structure."""

    entries: List[ActionEntry] = field(default_factory=list)

    def add_entry(self, entry: ActionEntry) -> None:
        """Add an action entry to the report."""
        self.entries.append(entry)
