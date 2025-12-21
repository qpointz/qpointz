"""Extensible version selection interface."""

from abc import ABC, abstractmethod
from typing import Dict, List


class VersionSelector(ABC):
    """Abstract base class for version selectors."""

    @abstractmethod
    def select_versions(self, versions: List[str], context: Dict) -> List[str]:
        """
        Select versions from a list based on strategy.

        Args:
            versions: List of version identifiers
            context: Context dictionary (may contain 'group' and other info)

        Returns:
            Filtered list of versions
        """
        pass


class AllVersionsSelector(VersionSelector):
    """Default selector that includes all versions."""

    def select_versions(self, versions: List[str], context: Dict) -> List[str]:
        """Return all versions unchanged."""
        return versions

