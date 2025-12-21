"""Base comparer interface for metrics."""

from abc import ABC, abstractmethod
from dataclasses import dataclass
from typing import Any, Optional


@dataclass
class MetricConfig:
    """Configuration for metric comparison."""

    metric_name: str
    threshold: Optional[float] = None
    threshold_type: str = "absolute"  # "absolute" or "relative"
    direction: str = "any"  # "less_is_better", "more_is_better", "equal", "any"
    warn_on_threshold_exceeded: bool = True
    tolerate_small_improvements: bool = False


@dataclass
class ComparisonResult:
    """Result of metric comparison."""

    delta: Any
    status: str  # "PASS", "WARN", or "FAIL"
    reason: Optional[str] = None
    is_significant_change: bool = False


class MetricComparer(ABC):
    """Abstract base class for metric comparers."""

    @abstractmethod
    def compare(
        self, baseline: Any, candidate: Any, config: MetricConfig
    ) -> ComparisonResult:
        """
        Compare baseline and candidate values.

        Args:
            baseline: Baseline value
            candidate: Candidate value
            config: Metric configuration

        Returns:
            ComparisonResult with delta, status, and reason
        """
        pass

    def _check_threshold(self, delta: float, config: MetricConfig) -> bool:
        """Check if delta exceeds threshold."""
        if config.threshold is None:
            return False

        if config.threshold_type == "relative":
            # For relative thresholds, need baseline value to calculate percentage
            # This is handled by numeric comparer
            return False

        # Absolute threshold
        abs_delta = abs(delta)
        return abs_delta > config.threshold

