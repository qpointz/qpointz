"""Multi-version diff table data models."""

from dataclasses import dataclass, field
from typing import List, Dict, Any, Optional


@dataclass
class MultiVersionDiffRow:
    """Single row in multi-version diff table."""

    step: str  # Step identifier
    metric: str  # Metric name (normalized)
    version_values: Dict[str, Any]  # Dictionary of version -> value
    absolute_changes: Dict[str, float] = field(default_factory=dict)  # Dictionary of "version_pair" -> absolute change (e.g., "0.5.0-dev → 0.5.0-poc-value-mapping-tool" -> 5.0)
    percent_changes: Dict[str, float] = field(default_factory=dict)  # Dictionary of "version_pair" -> percent change (e.g., "0.5.0-dev → 0.5.0-poc-value-mapping-tool" -> 10.5)
    is_numeric: bool = False  # Whether this metric is numeric
    status: str = "PASS"  # Overall status (PASS/WARN/FAIL)
    status_reason: Optional[str] = None  # Optional reason for status classification


@dataclass
class MultiVersionDiffTable:
    """Container for multi-version diff table."""

    group: str  # Group name (e.g., "ai")
    versions: List[str]  # List of version identifiers in order
    rows: List[MultiVersionDiffRow] = field(default_factory=list)  # List of diff rows
    metadata: Dict[str, Any] = field(
        default_factory=dict
    )  # Optional metadata (generation time, config used, etc.)

    def add_row(self, row: MultiVersionDiffRow) -> None:
        """Add a row to the table."""
        self.rows.append(row)

    def get_step_label(self, composite_key: tuple) -> str:
        """Generate step label from composite key."""
        # Format: <scenario> - <action>
        group, scenario, action_key, params_str = composite_key
        return f"{scenario} - {action_key}"

