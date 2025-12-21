"""Boolean metric comparer."""

from typing import Any
from regression_diff.metric_comparers.base import MetricComparer, MetricConfig, ComparisonResult


class BooleanComparer(MetricComparer):
    """Compare boolean values."""

    def compare(
        self, baseline: Any, candidate: Any, config: MetricConfig
    ) -> ComparisonResult:
        """Compare boolean values."""
        # Handle None values
        if baseline is None and candidate is None:
            return ComparisonResult(
                delta=None, status="PASS", reason="Both values are None"
            )

        if baseline is None or candidate is None:
            return ComparisonResult(
                delta=None,
                status="WARN",
                reason="One value is None",
            )

        # Convert to boolean
        baseline_bool = bool(baseline)
        candidate_bool = bool(candidate)

        # Delta: True if changed, False if same
        delta = baseline_bool != candidate_bool

        if not delta:
            return ComparisonResult(
                delta=False, status="PASS", reason="Boolean values match"
            )

        # Values differ
        if config.direction == "equal":
            if config.warn_on_threshold_exceeded:
                return ComparisonResult(
                    delta=True,
                    status="WARN",
                    reason=f"Boolean changed: {baseline_bool} -> {candidate_bool}",
                )
            else:
                return ComparisonResult(
                    delta=True,
                    status="FAIL",
                    reason=f"Boolean changed: {baseline_bool} -> {candidate_bool}",
                )

        # Direction is not "equal", so change is acceptable
        return ComparisonResult(
            delta=True,
            status="PASS",
            reason=f"Boolean changed: {baseline_bool} -> {candidate_bool}",
        )

