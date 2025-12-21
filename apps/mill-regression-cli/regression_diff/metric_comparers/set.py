"""Set/list metric comparer."""

from dataclasses import dataclass
from typing import Any, Set, List
from regression_diff.metric_comparers.base import MetricComparer, MetricConfig, ComparisonResult


@dataclass
class SetDelta:
    """Delta for set comparisons."""

    added: Set[Any]
    removed: Set[Any]


class SetComparer(MetricComparer):
    """Compare set/list values (order-independent)."""

    def compare(
        self, baseline: Any, candidate: Any, config: MetricConfig
    ) -> ComparisonResult:
        """Compare set/list values."""
        # Handle None values
        if baseline is None and candidate is None:
            return ComparisonResult(
                delta=None, status="PASS", reason="Both values are None"
            )

        if baseline is None:
            baseline_set: Set[Any] = set()
        elif isinstance(baseline, (list, tuple)):
            baseline_set = set(baseline)
        else:
            baseline_set = {baseline}

        if candidate is None:
            candidate_set: Set[Any] = set()
        elif isinstance(candidate, (list, tuple)):
            candidate_set = set(candidate)
        else:
            candidate_set = {candidate}

        # Calculate delta
        added = candidate_set - baseline_set
        removed = baseline_set - candidate_set
        delta = SetDelta(added=added, removed=removed)

        if not added and not removed:
            return ComparisonResult(
                delta=delta, status="PASS", reason="Sets match"
            )

        # Sets differ
        if config.direction == "equal":
            if config.warn_on_threshold_exceeded:
                return ComparisonResult(
                    delta=delta,
                    status="WARN",
                    reason=f"Set changed: added={len(added)}, removed={len(removed)}",
                )
            else:
                return ComparisonResult(
                    delta=delta,
                    status="FAIL",
                    reason=f"Set changed: added={len(added)}, removed={len(removed)}",
                )

        # Direction is not "equal", so change might be acceptable
        return ComparisonResult(
            delta=delta,
            status="PASS",
            reason=f"Set changed: added={len(added)}, removed={len(removed)}",
        )

