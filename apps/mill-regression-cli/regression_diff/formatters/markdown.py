"""Markdown formatter for diff tables."""

from typing import Any
from regression_diff.models import DiffTable, DiffRow
from regression_diff.formatters.base import Formatter


class MarkdownFormatter(Formatter):
    """Format DiffTable as Markdown table."""

    def format(self, diff_table: DiffTable) -> str:
        """Format DiffTable as Markdown."""
        lines: list[str] = []

        # Header
        header = f"### {diff_table.group} — {diff_table.baseline_version} → {diff_table.candidate_version}"
        lines.append(header)
        lines.append("")

        # Table header
        lines.append("| Step | Metric | Baseline | Candidate | Δ | Status |")
        lines.append("|------|--------|----------|-----------|---|--------|")

        # Table rows
        for row in diff_table.rows:
            step = self._escape_markdown(str(row.step))
            metric = self._escape_markdown(str(row.metric))
            baseline = self._format_value(row.baseline_value)
            candidate = self._format_value(row.candidate_value)
            delta = self._format_delta(row.delta)
            status = row.status

            lines.append(
                f"| {step} | {metric} | {baseline} | {candidate} | {delta} | {status} |"
            )

        # Add reason if present
        for row in diff_table.rows:
            if row.status_reason:
                lines.append("")
                lines.append(f"**{row.step} - {row.metric}**: {row.status_reason}")

        return "\n".join(lines)

    def _escape_markdown(self, text: str) -> str:
        """Escape markdown special characters."""
        return text.replace("|", "\\|").replace("\n", " ")

    def _format_value(self, value: Any) -> str:
        """Format a value for display."""
        if value is None:
            return "—"

        if isinstance(value, bool):
            return str(value)

        if isinstance(value, (int, float)):
            return str(value)

        if isinstance(value, (list, tuple, set)):
            if not value:
                return "[]"
            # Show first few items
            items = list(value)[:3]
            items_str = ", ".join(str(item) for item in items)
            if len(value) > 3:
                items_str += f", ... ({len(value)} total)"
            return f"[{items_str}]"

        # String or other
        text = str(value)
        if len(text) > 50:
            text = text[:47] + "..."
        return text

    def _format_delta(self, delta: Any) -> str:
        """Format delta value."""
        if delta is None:
            return "—"

        if isinstance(delta, (int, float)):
            if delta > 0:
                return f"+{delta}"
            return str(delta)

        if isinstance(delta, str):
            # String delta (e.g., "A -> B")
            if len(delta) > 30:
                return delta[:27] + "..."
            return delta

        # SetDelta or other complex types
        if hasattr(delta, "added") and hasattr(delta, "removed"):
            # SetDelta
            added = len(delta.added)
            removed = len(delta.removed)
            return f"+{added}/-{removed}"

        return str(delta)

