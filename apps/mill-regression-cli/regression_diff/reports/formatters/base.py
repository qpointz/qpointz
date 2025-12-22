"""Base formatter for metrics reports."""

from abc import ABC, abstractmethod
from regression_diff.reports.models import MetricsReport


class BaseMetricsFormatter(ABC):
    """Abstract base class for metrics report formatters."""

    @abstractmethod
    def format(self, report: MetricsReport) -> str:
        """
        Format a MetricsReport into a string representation.
        
        Args:
            report: MetricsReport to format
        
        Returns:
            Formatted string
        """
        pass
