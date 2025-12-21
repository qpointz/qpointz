"""Comparer registry for metric type mapping."""

from typing import Any, Dict, Optional
from regression_diff.metric_comparers.base import MetricComparer
from regression_diff.metric_comparers.numeric import NumericComparer
from regression_diff.metric_comparers.boolean import BooleanComparer
from regression_diff.metric_comparers.string import StringComparer
from regression_diff.metric_comparers.set import SetComparer


class ComparerRegistry:
    """Registry for metric comparers."""

    def __init__(self):
        """Initialize registry with default comparers."""
        self._default_comparers: Dict[type, MetricComparer] = {
            int: NumericComparer(),
            float: NumericComparer(),
            bool: BooleanComparer(),
            str: StringComparer(),
            list: SetComparer(),
            set: SetComparer(),
            tuple: SetComparer(),
        }

        # Metric name patterns -> comparer
        self._pattern_comparers: Dict[str, MetricComparer] = {}

    def register_pattern(self, pattern: str, comparer: MetricComparer) -> None:
        """Register a comparer for a metric name pattern."""
        self._pattern_comparers[pattern] = comparer

    def get_comparer(self, metric_name: str, value_type: type) -> MetricComparer:
        """
        Get appropriate comparer for metric.

        Args:
            metric_name: Name of the metric
            value_type: Type of the metric value

        Returns:
            Appropriate MetricComparer instance
        """
        # Check pattern matches first
        for pattern, comparer in self._pattern_comparers.items():
            if pattern in metric_name or metric_name.startswith(pattern):
                return comparer

        # Check type-based mapping
        if value_type in self._default_comparers:
            return self._default_comparers[value_type]

        # Check for numeric types
        if value_type in (int, float) or (
            hasattr(value_type, "__int__") or hasattr(value_type, "__float__")
        ):
            return NumericComparer()

        # Default to string comparer for unknown types
        return StringComparer()

    def get_comparer_for_value(
        self, metric_name: str, value: Any
    ) -> MetricComparer:
        """Get comparer based on actual value type."""
        if value is None:
            # Can't determine type from None, use metric name pattern or default to string
            for pattern, comparer in self._pattern_comparers.items():
                if pattern in metric_name:
                    return comparer
            return StringComparer()

        value_type = type(value)
        return self.get_comparer(metric_name, value_type)


# Global registry instance
_default_registry = ComparerRegistry()


def get_default_registry() -> ComparerRegistry:
    """Get the default comparer registry."""
    return _default_registry

