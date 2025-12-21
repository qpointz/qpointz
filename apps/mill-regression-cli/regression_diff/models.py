"""Format-agnostic diff table data models."""

from dataclasses import dataclass, field
from typing import List, Dict, Any, Optional


@dataclass
class DiffRow:
    """Single row in diff table."""

    step: str  # Step identifier (derived from ask action params.value or composite key)
    metric: str  # Metric name (normalized)
    baseline_value: Any  # Baseline value (preserve original type)
    candidate_value: Any  # Candidate value (preserve original type)
    delta: Optional[Any] = None  # Delta value (if numeric/metric supports it)
    status: str = "PASS"  # Status (PASS/WARN/FAIL)
    status_reason: Optional[str] = None  # Optional reason for status classification


@dataclass
class DiffTable:
    """Container for entire diff table."""

    group: str  # Group name (e.g., "ai")
    baseline_version: str  # Baseline version identifier
    candidate_version: str  # Candidate version identifier
    rows: List[DiffRow] = field(default_factory=list)  # List of diff rows
    metadata: Dict[str, Any] = field(
        default_factory=dict
    )  # Optional metadata (generation time, config used, etc.)

    def add_row(self, row: DiffRow) -> None:
        """Add a row to the table."""
        self.rows.append(row)

    def get_step_label(self, composite_key: tuple) -> str:
        """Generate step label from composite key."""
        # Format: <scenario> - <action>
        group, scenario, action_key, params_str = composite_key
        return f"{scenario} - {action_key}"

