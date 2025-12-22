"""JSON formatter for multi-version diff tables."""

import json
from typing import Any, Dict, Union
from regression_diff.models_multi import MultiVersionDiffTable, MultiVersionDiffRow
from regression_diff.models_action_grouped import ActionGroupedReport, ActionEntry, VersionAction
from regression_diff.formatters.base import Formatter


class JSONMultiVersionFormatter(Formatter):
    """Format MultiVersionDiffTable or ActionGroupedReport as JSON."""

    def __init__(self, pretty: bool = True):
        """Initialize JSON formatter.

        Args:
            pretty: Whether to pretty-print JSON (indented)
        """
        self.pretty = pretty

    def format(self, data: Union[MultiVersionDiffTable, ActionGroupedReport]) -> str:
        """Format MultiVersionDiffTable or ActionGroupedReport as JSON."""
        if isinstance(data, ActionGroupedReport):
            json_data = self._action_grouped_to_dict(data)
        else:
            json_data = self._diff_table_to_dict(data)

        if self.pretty:
            return json.dumps(json_data, indent=2, default=self._json_serializer)
        else:
            return json.dumps(json_data, default=self._json_serializer)

    def _action_grouped_to_dict(self, report: ActionGroupedReport) -> list:
        """Convert ActionGroupedReport to dictionary (array format)."""
        result = []
        for entry in report.entries:
            entry_dict = {
                "action": entry.action,
                "versions": [
                    {
                        "version": va.version,
                        "action": va.action,
                    }
                    for va in entry.versions
                ],
            }
            result.append(entry_dict)
        return result

    def _diff_table_to_dict(self, diff_table: MultiVersionDiffTable) -> Dict[str, Any]:
        """Convert MultiVersionDiffTable to dictionary."""
        return {
            "group": diff_table.group,
            "versions": diff_table.versions,
            "rows": [self._diff_row_to_dict(row) for row in diff_table.rows],
            "metadata": diff_table.metadata,
        }

    def _diff_row_to_dict(self, row: MultiVersionDiffRow) -> Dict[str, Any]:
        """Convert MultiVersionDiffRow to dictionary."""
        result = {
            "step": row.step,
            "metric": row.metric,
            "version_values": row.version_values,
            "is_numeric": row.is_numeric,
            "status": row.status,
        }

        if row.absolute_changes:
            result["absolute_changes"] = row.absolute_changes

        if row.percent_changes:
            # Convert infinity to string for JSON serialization
            percent_changes_serialized = {}
            for key, value in row.percent_changes.items():
                if value == float('inf'):
                    percent_changes_serialized[key] = "inf"
                elif value == float('-inf'):
                    percent_changes_serialized[key] = "-inf"
                else:
                    percent_changes_serialized[key] = value
            result["percent_changes"] = percent_changes_serialized

        if row.status_reason:
            result["status_reason"] = row.status_reason

        return result

    def _json_serializer(self, obj: Any) -> Any:
        """Custom JSON serializer for complex types."""
        if hasattr(obj, "added") and hasattr(obj, "removed"):
            # SetDelta
            return {"added": list(obj.added), "removed": list(obj.removed)}
        if isinstance(obj, (set, frozenset)):
            return list(obj)

        raise TypeError(f"Type {type(obj)} not serializable")
