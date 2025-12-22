"""Report data models."""

from dataclasses import dataclass, field
from typing import Dict, Any, List


@dataclass
class MetricRow:
    """Represents a single row in the metrics report."""

    group: str
    scenario: str
    action: str  # Action key (e.g., "ask")
    metric: str  # Metric path (e.g., "action.outcome.metrics.execution.time")
    version_values: Dict[str, Any] = field(default_factory=dict)  # version -> value


@dataclass
class MetricsReport:
    """Container for metrics report data."""

    rows: List[MetricRow] = field(default_factory=list)
    versions: List[str] = field(default_factory=list)  # Ordered list of versions

    def add_row(self, row: MetricRow) -> None:
        """Add a metric row to the report."""
        self.rows.append(row)
