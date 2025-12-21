"""CSV formatter for multi-version diff tables."""

import csv
import io
from typing import Any
from regression_diff.models_multi import MultiVersionDiffTable, MultiVersionDiffRow
from regression_diff.formatters.base import Formatter


class CSVMultiVersionFormatter(Formatter):
    """Format MultiVersionDiffTable as CSV."""

    def format(self, diff_table: MultiVersionDiffTable) -> str:
        """Formats a MultiVersionDiffTable object into a CSV string."""
        output = io.StringIO()
        writer = csv.writer(output)

        # Build header row with version pairs for changes
        header = ["Group", "Step", "Metric"] + diff_table.versions
        # Add absolute and percent change columns for each adjacent version pair
        change_headers = []
        for i in range(len(diff_table.versions) - 1):
            prev_version = diff_table.versions[i]
            next_version = diff_table.versions[i + 1]
            change_headers.append(f"{prev_version} → {next_version} (abs)")
            change_headers.append(f"{prev_version} → {next_version} (%)")
        header = header + change_headers + ["Status", "Reason"]
        writer.writerow(header)

        # Data rows
        for row in diff_table.rows:
            csv_row = [
                diff_table.group,
                row.step,
                row.metric,
            ]
            # Add value for each version
            for version in diff_table.versions:
                value = row.version_values.get(version)
                csv_row.append(self._format_value(value))
            
            # Add absolute and percent changes for each version pair (only for numeric metrics)
            for i in range(len(diff_table.versions) - 1):
                prev_version = diff_table.versions[i]
                next_version = diff_table.versions[i + 1]
                pair_key = f"{prev_version} → {next_version}"
                
                if row.is_numeric and pair_key in row.absolute_changes:
                    abs_change = row.absolute_changes[pair_key]
                    csv_row.append(f"{abs_change:.2f}")
                else:
                    csv_row.append("")
                
                if row.is_numeric and pair_key in row.percent_changes:
                    pct_change = row.percent_changes[pair_key]
                    if pct_change == float('inf'):
                        csv_row.append("∞")
                    elif pct_change == float('-inf'):
                        csv_row.append("-∞")
                    else:
                        csv_row.append(f"{pct_change:.2f}%")
                else:
                    csv_row.append("")
            
            # Add status and reason
            csv_row.append(row.status)
            csv_row.append(row.status_reason if row.status_reason else "")
            
            writer.writerow(csv_row)

        return output.getvalue()

    def _format_value(self, value: Any) -> str:
        """Formats a value for CSV output."""
        if value is None:
            return ""
        if isinstance(value, bool):
            return str(value).lower()
        if isinstance(value, (list, set, tuple)):
            # Join list/set items with a comma
            return ", ".join(sorted(map(str, value)))
        if isinstance(value, float):
            return f"{value:.2f}"
        return str(value)

