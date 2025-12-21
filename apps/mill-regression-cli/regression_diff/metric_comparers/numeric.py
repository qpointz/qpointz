"""Numeric metric comparer."""

from typing import Any
from regression_diff.metric_comparers.base import MetricComparer, MetricConfig, ComparisonResult


class NumericComparer(MetricComparer):
    """Compare numeric values with thresholds and direction preferences."""

    def compare(
        self, baseline: Any, candidate: Any, config: MetricConfig
    ) -> ComparisonResult:
        """Compare numeric values."""
        # Handle None values
        if baseline is None and candidate is None:
            return ComparisonResult(
                delta=0, status="PASS", reason="Both values are None"
            )

        if baseline is None:
            return ComparisonResult(
                delta=None,
                status="WARN",
                reason="Baseline is None, candidate has value",
            )

        if candidate is None:
            return ComparisonResult(
                delta=None, status="WARN", reason="Candidate is None, baseline has value"
            )

        # Convert to float for comparison
        try:
            baseline_val = float(baseline)
            candidate_val = float(candidate)
        except (ValueError, TypeError):
            return ComparisonResult(
                delta=None,
                status="FAIL",
                reason=f"Cannot convert to numeric: baseline={baseline}, candidate={candidate}",
            )

        # Calculate delta
        delta = candidate_val - baseline_val

        # Determine if change is significant
        is_significant = self._check_threshold(delta, baseline_val, config)

        # Determine status based on direction
        status, reason = self._determine_status(delta, is_significant, config)

        return ComparisonResult(
            delta=delta,
            status=status,
            reason=reason,
            is_significant_change=is_significant,
        )

    def _check_threshold(
        self, delta: float, baseline: float, config: MetricConfig
    ) -> bool:
        """Check if delta exceeds threshold (absolute or relative)."""
        if config.threshold is None:
            return False

        abs_delta = abs(delta)

        if config.threshold_type == "relative":
            if baseline == 0:
                # Can't calculate relative change from zero
                return abs_delta > 0
            relative_change = abs_delta / abs(baseline) * 100
            return relative_change > config.threshold
        else:
            # Absolute threshold
            return abs_delta > config.threshold

    def _determine_status(
        self, delta: float, is_significant: bool, config: MetricConfig
    ) -> tuple[str, str]:
        """Determine status based on direction and threshold."""
        if delta == 0:
            return ("PASS", "No change")

        direction_ok = self._check_direction(delta, config)

        if not direction_ok:
            if config.warn_on_threshold_exceeded:
                return ("WARN", f"Change in wrong direction: {delta:.2f}")
            else:
                return ("FAIL", f"Change in wrong direction: {delta:.2f}")

        if is_significant:
            if config.warn_on_threshold_exceeded:
                return (
                    "WARN",
                    f"Significant change exceeds threshold: {delta:.2f}",
                )
            else:
                return (
                    "FAIL",
                    f"Significant change exceeds threshold: {delta:.2f}",
                )

        # Change in correct direction and within threshold
        if config.direction == "less_is_better" and delta < 0:
            return ("PASS", f"Improvement: {delta:.2f}")
        elif config.direction == "more_is_better" and delta > 0:
            return ("PASS", f"Improvement: {delta:.2f}")
        else:
            return ("PASS", f"Change within threshold: {delta:.2f}")

    def _check_direction(self, delta: float, config: MetricConfig) -> bool:
        """Check if delta direction matches expected direction."""
        if config.direction == "any":
            return True

        if config.direction == "equal":
            return delta == 0

        if config.direction == "less_is_better":
            return delta <= 0 or (config.tolerate_small_improvements and delta > 0)

        if config.direction == "more_is_better":
            return delta >= 0 or (config.tolerate_small_improvements and delta < 0)

        return True

