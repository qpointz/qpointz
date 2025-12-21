"""Markdown formatter for multi-version diff tables."""

from typing import Any
from regression_diff.models_multi import MultiVersionDiffTable, MultiVersionDiffRow
from regression_diff.formatters.base import Formatter


class MarkdownMultiVersionFormatter(Formatter):
    """Format MultiVersionDiffTable as Markdown table."""

    def format(self, diff_table: MultiVersionDiffTable) -> str:
        """Formats a MultiVersionDiffTable object into a Markdown string."""
        output = []

        # Header
        output.append(f"### {diff_table.group} — Multi-Version Comparison\n")

        if not diff_table.rows:
            output.append("No data available.\n")
            return "\n".join(output)

        # Build table header with change columns
        header_parts = ["Step", "Metric"] + diff_table.versions
        # Add absolute and percent change columns for each adjacent version pair
        for i in range(len(diff_table.versions) - 1):
            prev_version = diff_table.versions[i]
            next_version = diff_table.versions[i + 1]
            header_parts.append(f"{prev_version} → {next_version}<br/>Δ (abs)")
            header_parts.append(f"{prev_version} → {next_version}<br/>Δ (%)")
        header_parts.append("Status")
        output.append("| " + " | ".join(header_parts) + " |")
        
        # Separator row
        num_columns = len(diff_table.versions) + 2 + (len(diff_table.versions) - 1) * 2 + 1
        separator_parts = ["------"] * num_columns
        output.append("|" + "|".join(separator_parts) + "|")

        # Table rows
        for row in diff_table.rows:
            row_parts = [
                row.step,
                row.metric,
            ]
            # Add value for each version
            for version in diff_table.versions:
                value = row.version_values.get(version)
                row_parts.append(self._format_value(value))
            
            # Add absolute and percent changes for each version pair (only for numeric metrics)
            for i in range(len(diff_table.versions) - 1):
                prev_version = diff_table.versions[i]
                next_version = diff_table.versions[i + 1]
                pair_key = f"{prev_version} → {next_version}"
                
                if row.is_numeric and pair_key in row.absolute_changes:
                    abs_change = row.absolute_changes[pair_key]
                    row_parts.append(f"{abs_change:.2f}")
                else:
                    row_parts.append("—")
                
                if row.is_numeric and pair_key in row.percent_changes:
                    pct_change = row.percent_changes[pair_key]
                    if pct_change == float('inf'):
                        row_parts.append("∞")
                    elif pct_change == float('-inf'):
                        row_parts.append("-∞")
                    else:
                        row_parts.append(f"{pct_change:.2f}%")
                else:
                    row_parts.append("—")
            
            # Add status
            row_parts.append(row.status)
            
            output.append("| " + " | ".join(row_parts) + " |")

        return "\n".join(output)

    def _format_value(self, value: Any) -> str:
        """Formats a value for display in Markdown."""
        if value is None:
            return "—"
        if isinstance(value, bool):
            return "✅" if value else "❌"
        if isinstance(value, (list, set, tuple)):
            if not value:
                return "[]"
            # Sort for consistent output
            return ", ".join(sorted(map(str, value)))
        if isinstance(value, float):
            return f"{value:.2f}"
        if isinstance(value, int):
            return str(value)
        return str(value)

