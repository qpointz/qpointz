"""Comparator orchestrator using modular metric comparers."""

from typing import Dict, List, Any, Optional
from regression_diff.matcher import MatchedPair, MatchingResult
from regression_diff.metric_comparers.registry import get_default_registry, ComparerRegistry
from regression_diff.metric_comparers.base import MetricConfig
from regression_diff.config import Config


class MetricComparison:
    """Result of comparing a single metric."""

    def __init__(
        self,
        metric_name: str,
        baseline_value: Any,
        candidate_value: Any,
        delta: Any,
        status: str,
        reason: Optional[str] = None,
    ):
        """Initialize metric comparison."""
        self.metric_name = metric_name
        self.baseline_value = baseline_value
        self.candidate_value = candidate_value
        self.delta = delta
        self.status = status
        self.reason = reason


def compare_matched_pair(
    matched_pair: MatchedPair,
    config: Config,
    comparer_registry: Optional[ComparerRegistry] = None,
) -> List[MetricComparison]:
    """
    Compare all metrics in a matched pair.

    Args:
        matched_pair: Matched pair to compare
        config: Configuration with metric configs
        comparer_registry: Optional comparer registry (uses default if None)

    Returns:
        List of MetricComparison results
    """
    if comparer_registry is None:
        comparer_registry = get_default_registry()

    comparisons: List[MetricComparison] = []

    baseline_metrics = matched_pair.baseline_metrics
    candidate_metrics = matched_pair.candidate_metrics

    # Get normalized dictionaries
    from regression_diff.normalizer import normalize_to_dict

    baseline_dict = normalize_to_dict(baseline_metrics)
    candidate_dict = normalize_to_dict(candidate_metrics)

    # Compare each metric
    all_metric_names = set(baseline_dict.keys()) | set(candidate_dict.keys())

    for metric_name in sorted(all_metric_names):
        baseline_value = baseline_dict.get(metric_name)
        candidate_value = candidate_dict.get(metric_name)

        # Get metric config
        metric_config = config.get_metric_config(metric_name)

        # Get appropriate comparer
        comparer = comparer_registry.get_comparer_for_value(
            metric_name, baseline_value if baseline_value is not None else candidate_value
        )

        # Compare
        comparison_result = comparer.compare(
            baseline_value, candidate_value, metric_config
        )

        comparison = MetricComparison(
            metric_name=metric_name,
            baseline_value=baseline_value,
            candidate_value=candidate_value,
            delta=comparison_result.delta,
            status=comparison_result.status,
            reason=comparison_result.reason,
        )

        comparisons.append(comparison)

    return comparisons


def compare_results(
    matching_result: MatchingResult,
    config: Config,
    comparer_registry: Optional[ComparerRegistry] = None,
) -> Dict[str, List[MetricComparison]]:
    """
    Compare all matched pairs and generate comparison results.

    Args:
        matching_result: Result of matching baseline and candidate
        config: Configuration
        comparer_registry: Optional comparer registry

    Returns:
        Dictionary mapping composite_key (as string) -> List[MetricComparison]
    """
    results: Dict[str, List[MetricComparison]] = {}

    # Compare matched pairs
    for matched_pair in matching_result.matched_pairs:
        key_str = str(matched_pair.composite_key)
        comparisons = compare_matched_pair(matched_pair, config, comparer_registry)
        results[key_str] = comparisons

    return results

