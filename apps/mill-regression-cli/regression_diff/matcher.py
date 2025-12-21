"""Composite key matching between baseline and candidate versions."""

from typing import Dict, List, Tuple, Set
from regression_diff.parser import ParsedResult
from regression_diff.normalizer import NormalizedMetrics, normalize_result


class MatchedPair:
    """Represents a matched pair of baseline and candidate results."""

    def __init__(
        self,
        composite_key: Tuple[str, str, str, str],
        baseline_result: ParsedResult,
        candidate_result: ParsedResult,
        baseline_metrics: NormalizedMetrics,
        candidate_metrics: NormalizedMetrics,
    ):
        """Initialize matched pair."""
        self.composite_key = composite_key
        self.baseline_result = baseline_result
        self.candidate_result = candidate_result
        self.baseline_metrics = baseline_metrics
        self.candidate_metrics = candidate_metrics


class MatchingResult:
    """Result of matching baseline and candidate results."""

    def __init__(self):
        """Initialize empty matching result."""
        self.matched_pairs: List[MatchedPair] = []
        self.unmatched_baseline: List[Tuple[Tuple[str, str, str, str], ParsedResult]] = []
        self.unmatched_candidate: List[Tuple[Tuple[str, str, str, str], ParsedResult]] = []


def match_results(
    baseline_results: Dict[Tuple[str, str, str, str], ParsedResult],
    candidate_results: Dict[Tuple[str, str, str, str], ParsedResult],
) -> MatchingResult:
    """
    Match results between baseline and candidate by composite key.

    Args:
        baseline_results: Dictionary of composite_key -> ParsedResult for baseline
        candidate_results: Dictionary of composite_key -> ParsedResult for candidate

    Returns:
        MatchingResult with matched pairs and unmatched results
    """
    result = MatchingResult()

    # Normalize metrics for all results
    baseline_metrics_map: Dict[Tuple[str, str, str, str], NormalizedMetrics] = {}
    for key, parsed_result in baseline_results.items():
        baseline_metrics_map[key] = normalize_result(parsed_result)

    candidate_metrics_map: Dict[Tuple[str, str, str, str], NormalizedMetrics] = {}
    for key, parsed_result in candidate_results.items():
        candidate_metrics_map[key] = normalize_result(parsed_result)

    # Find matched pairs
    baseline_keys: Set[Tuple[str, str, str, str]] = set(baseline_results.keys())
    candidate_keys: Set[Tuple[str, str, str, str]] = set(candidate_results.keys())

    matched_keys = baseline_keys & candidate_keys

    for key in matched_keys:
        matched_pair = MatchedPair(
            composite_key=key,
            baseline_result=baseline_results[key],
            candidate_result=candidate_results[key],
            baseline_metrics=baseline_metrics_map[key],
            candidate_metrics=candidate_metrics_map[key],
        )
        result.matched_pairs.append(matched_pair)

    # Find unmatched baseline (removed in candidate)
    unmatched_baseline_keys = baseline_keys - candidate_keys
    for key in unmatched_baseline_keys:
        result.unmatched_baseline.append((key, baseline_results[key]))

    # Find unmatched candidate (added in candidate)
    unmatched_candidate_keys = candidate_keys - baseline_keys
    for key in unmatched_candidate_keys:
        result.unmatched_candidate.append((key, candidate_results[key]))

    return result

