"""CSV formatter for diff tables."""

import csv
import io
from regression_diff.models import DiffTable
from regression_diff.formatters.base import Formatter


class CSVFormatter(Formatter):
    """Format DiffTable as CSV."""

    def format(self, diff_table: DiffTable) -> str:
        """Format DiffTable as CSV."""
        output = io.StringIO()
        writer = csv.writer(output)

        # Header row
        writer.writerow(
            ["Step", "Metric", "Baseline", "Candidate", "Delta", "Status"]
        )

        # Data rows
        for row in diff_table.rows:
            writer.writerow(
                [
                    str(row.step),
                    str(row.metric),
                    self._format_value(row.baseline_value),
                    self._format_value(row.candidate_value),
                    self._format_delta(row.delta),
                    row.status,
                ]
            )

        return output.getvalue()

    def _format_value(self, value) -> str:
        """Format a value for CSV."""
        if value is None:
            return ""

        if isinstance(value, (list, tuple, set)):
            return ",".join(str(item) for item in value)

        return str(value)

    def _format_delta(self, delta) -> str:
        """Format delta value."""
        if delta is None:
            return ""

        if hasattr(delta, "added") and hasattr(delta, "removed"):
            # SetDelta
            return f"+{len(delta.added)}/-{len(delta.removed)}"

        return str(delta)

