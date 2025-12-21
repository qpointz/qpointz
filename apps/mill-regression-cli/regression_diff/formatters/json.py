"""JSON formatter for diff tables."""

import json
from typing import Any, Dict
from regression_diff.models import DiffTable, DiffRow
from regression_diff.formatters.base import Formatter


class JSONFormatter(Formatter):
    """Format DiffTable as JSON."""

    def __init__(self, pretty: bool = True):
        """Initialize JSON formatter.

        Args:
            pretty: Whether to pretty-print JSON (indented)
        """
        self.pretty = pretty

    def format(self, diff_table: DiffTable) -> str:
        """Format DiffTable as JSON."""
        data = self._diff_table_to_dict(diff_table)

        if self.pretty:
            return json.dumps(data, indent=2, default=self._json_serializer)
        else:
            return json.dumps(data, default=self._json_serializer)

    def _diff_table_to_dict(self, diff_table: DiffTable) -> Dict[str, Any]:
        """Convert DiffTable to dictionary."""
        return {
            "group": diff_table.group,
            "baseline_version": diff_table.baseline_version,
            "candidate_version": diff_table.candidate_version,
            "rows": [self._diff_row_to_dict(row) for row in diff_table.rows],
            "metadata": diff_table.metadata,
        }

    def _diff_row_to_dict(self, row: DiffRow) -> Dict[str, Any]:
        """Convert DiffRow to dictionary."""
        result = {
            "step": row.step,
            "metric": row.metric,
            "baseline_value": row.baseline_value,
            "candidate_value": row.candidate_value,
            "status": row.status,
        }

        if row.delta is not None:
            result["delta"] = row.delta

        if row.status_reason:
            result["status_reason"] = row.status_reason

        return result

    def _json_serializer(self, obj: Any) -> Any:
        """Custom JSON serializer for complex types."""
        if hasattr(obj, "added") and hasattr(obj, "removed"):
            # SetDelta
            return {"added": list(obj.added), "removed": list(obj.removed)}

        raise TypeError(f"Type {type(obj)} not serializable")

