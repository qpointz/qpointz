"""PASS/WARN/FAIL classification based on comparison results."""

from typing import List, Optional
from regression_diff.matcher import MatchedPair, MatchingResult
from regression_diff.comparator import MetricComparison
from regression_diff.config import Config


class ClassificationResult:
    """Final classification result for a comparison."""

    def __init__(
        self,
        status: str,  # "PASS", "WARN", or "FAIL"
        reason: Optional[str] = None,
        metric_results: Optional[List[MetricComparison]] = None,
        is_unmatched_baseline: bool = False,
        is_unmatched_candidate: bool = False,
    ):
        """Initialize classification result."""
        self.status = status
        self.reason = reason
        self.metric_results = metric_results or []
        self.is_unmatched_baseline = is_unmatched_baseline
        self.is_unmatched_candidate = is_unmatched_candidate


def classify_matched_pair(
    matched_pair: MatchedPair,
    metric_comparisons: List[MetricComparison],
    config: Config,
) -> ClassificationResult:
    """
    Classify a matched pair based on metric comparisons and contract checks.

    Args:
        matched_pair: Matched pair
        metric_comparisons: List of metric comparison results
        config: Configuration

    Returns:
        ClassificationResult with overall status
    """
    reasons: List[str] = []
    statuses: List[str] = []

    # Check metric comparer results
    for comparison in metric_comparisons:
        statuses.append(comparison.status)
        if comparison.reason:
            reasons.append(f"{comparison.metric_name}: {comparison.reason}")

    # Check contract violations
    baseline_metrics = matched_pair.baseline_metrics
    candidate_metrics = matched_pair.candidate_metrics

    # Intent change check
    if baseline_metrics.intent != candidate_metrics.intent:
        statuses.append("FAIL")
        reasons.append(
            f"Intent changed: {baseline_metrics.intent} -> {candidate_metrics.intent}"
        )

    # Check for missing required artifacts
    # SQL presence check
    if baseline_metrics.has_sql and not candidate_metrics.has_sql:
        statuses.append("FAIL")
        reasons.append("SQL missing in candidate (was present in baseline)")

    # Data presence check
    if baseline_metrics.has_data and not candidate_metrics.has_data:
        statuses.append("WARN")
        reasons.append("Data missing in candidate (was present in baseline)")

    # Determine overall status
    if "FAIL" in statuses:
        overall_status = "FAIL"
    elif "WARN" in statuses:
        overall_status = "WARN"
    else:
        overall_status = "PASS"

    reason_str = "; ".join(reasons) if reasons else None

    return ClassificationResult(
        status=overall_status,
        reason=reason_str,
        metric_results=metric_comparisons,
    )


def classify_unmatched_baseline(
    composite_key: tuple, parsed_result, config: Config
) -> ClassificationResult:
    """Classify unmatched baseline result (removed in candidate)."""
    return ClassificationResult(
        status="FAIL",
        reason="Result present in baseline but missing in candidate",
        is_unmatched_baseline=True,
    )


def classify_unmatched_candidate(
    composite_key: tuple, parsed_result, config: Config
) -> ClassificationResult:
    """Classify unmatched candidate result (added in candidate)."""
    return ClassificationResult(
        status="WARN",
        reason="Result present in candidate but missing in baseline (new addition)",
        is_unmatched_candidate=True,
    )

