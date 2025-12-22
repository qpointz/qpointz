"""CSV formatter for metrics reports."""

import csv
import io
from typing import Any
from regression_diff.reports.models import MetricsReport
from regression_diff.reports.formatters.base import BaseMetricsFormatter


class CSVMetricsFormatter(BaseMetricsFormatter):
    """Format MetricsReport as CSV."""

    def format(self, report: MetricsReport) -> str:
        """Format MetricsReport as CSV string."""
        output = io.StringIO()
        writer = csv.writer(output, lineterminator="\n")
        
        # Build header: group,scenario,action,metric,v1,v2,...,vN
        header = ["group", "scenario", "action", "metric"] + report.versions
        writer.writerow(header)
        
        # Write data rows
        for row in report.rows:
            csv_row = [
                row.group,
                row.scenario,
                row.action,
                row.metric,
            ]
            # Add value for each version
            for version in report.versions:
                value = row.version_values.get(version)
                csv_row.append(self._format_value(value))
            
            writer.writerow(csv_row)
        
        return output.getvalue()

    def _format_value(self, value: Any) -> str:
        """Format a value for CSV output."""
        if value is None:
            return ""
        if isinstance(value, bool):
            return str(value).lower()
        if isinstance(value, (list, set, tuple)):
            # Join list/set items with a comma
            return ", ".join(map(str, value))
        if isinstance(value, float):
            return f"{value:.2f}"
        return str(value)
