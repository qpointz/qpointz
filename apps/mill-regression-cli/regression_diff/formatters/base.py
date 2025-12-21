"""Base formatter interface."""

from abc import ABC, abstractmethod
from regression_diff.models import DiffTable


class Formatter(ABC):
    """Abstract base class for formatters."""

    @abstractmethod
    def format(self, diff_table: DiffTable) -> str:
        """
        Format a DiffTable into a string representation.

        Args:
            diff_table: DiffTable to format

        Returns:
            Formatted string
        """
        pass

