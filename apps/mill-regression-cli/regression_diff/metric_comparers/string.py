"""String metric comparer."""

from typing import Any
from regression_diff.metric_comparers.base import MetricComparer, MetricConfig, ComparisonResult


class StringComparer(MetricComparer):
    """Compare string values."""

    def compare(
        self, baseline: Any, candidate: Any, config: MetricConfig
    ) -> ComparisonResult:
        """Compare string values."""
        # Handle None values
        if baseline is None and candidate is None:
            return ComparisonResult(
                delta=None, status="PASS", reason="Both values are None"
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

        # Convert to string
        baseline_str = str(baseline)
        candidate_str = str(candidate)

        if baseline_str == candidate_str:
            return ComparisonResult(
                delta=None, status="PASS", reason="String values match"
            )

        # Values differ
        delta = f"{baseline_str} -> {candidate_str}"

        if config.direction == "equal":
            if config.warn_on_threshold_exceeded:
                return ComparisonResult(
                    delta=delta,
                    status="WARN",
                    reason=f"String changed: {delta}",
                )
            else:
                return ComparisonResult(
                    delta=delta,
                    status="FAIL",
                    reason=f"String changed: {delta}",
                )

        # Direction is not "equal", so change might be acceptable
        return ComparisonResult(
            delta=delta,
            status="PASS",
            reason=f"String changed: {delta}",
        )

