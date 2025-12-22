"""Base formatter interface."""

from abc import ABC, abstractmethod
from typing import Any


class Formatter(ABC):
    """Abstract base class for formatters."""

    @abstractmethod
    def format(self, data: Any) -> str:
        """
        Format data into a string representation.

        Args:
            data: Data to format (MultiVersionDiffTable, ActionGroupedReport, etc.)

        Returns:
            Formatted string
        """
        pass

