"""Builder to create DiffTable models from comparisons."""

from typing import Dict, List
from regression_diff.matcher import MatchingResult
from regression_diff.comparator import compare_results, MetricComparison
from regression_diff.classifier import (
    classify_matched_pair,
    classify_unmatched_baseline,
    classify_unmatched_candidate,
)
from regression_diff.models import DiffTable, DiffRow
from regression_diff.config import Config


def build_diff_table(
    group: str,
    baseline_version: str,
    candidate_version: str,
    matching_result: MatchingResult,
    metric_comparisons: Dict[str, List[MetricComparison]],
    config: Config,
) -> DiffTable:
    """
    Build DiffTable model from matching and comparison results.

    Args:
        group: Group name
        baseline_version: Baseline version identifier
        candidate_version: Candidate version identifier
        matching_result: Result of matching baseline and candidate
        metric_comparisons: Dictionary mapping composite_key -> List[MetricComparison]
        config: Configuration

    Returns:
        DiffTable instance
    """
    diff_table = DiffTable(
        group=group,
        baseline_version=baseline_version,
        candidate_version=candidate_version,
    )

    # Process matched pairs
    for matched_pair in matching_result.matched_pairs:
        key_str = str(matched_pair.composite_key)
        comparisons = metric_comparisons.get(key_str, [])

        # Classify the matched pair
        classification = classify_matched_pair(
            matched_pair, comparisons, config
        )

        # Get step label
        step_label = (
            matched_pair.baseline_metrics.step_label
            or matched_pair.candidate_metrics.step_label
            or diff_table.get_step_label(matched_pair.composite_key)
        )

        # Create rows for each metric comparison
        for comparison in comparisons:
            row = DiffRow(
                step=step_label,
                metric=comparison.metric_name,
                baseline_value=comparison.baseline_value,
                candidate_value=comparison.candidate_value,
                delta=comparison.delta,
                status=comparison.status,
                status_reason=comparison.reason,
            )
            diff_table.add_row(row)

        # If no metric comparisons but we have a classification, add a summary row
        if not comparisons and classification.reason:
            row = DiffRow(
                step=step_label,
                metric="overall",
                baseline_value=None,
                candidate_value=None,
                delta=None,
                status=classification.status,
                status_reason=classification.reason,
            )
            diff_table.add_row(row)

    # Process unmatched baseline (removed)
    for composite_key, parsed_result in matching_result.unmatched_baseline:
        classification = classify_unmatched_baseline(
            composite_key, parsed_result, config
        )
        step_label = diff_table.get_step_label(composite_key)

        row = DiffRow(
            step=step_label,
            metric="presence",
            baseline_value=True,
            candidate_value=False,
            delta=None,
            status=classification.status,
            status_reason=classification.reason,
        )
        diff_table.add_row(row)

    # Process unmatched candidate (added) - show metrics, not just presence
    from regression_diff.normalizer import normalize_result, normalize_to_dict
    
    for composite_key, parsed_result in matching_result.unmatched_candidate:
        classification = classify_unmatched_candidate(
            composite_key, parsed_result, config
        )
        candidate_metrics = normalize_result(parsed_result)
        step_label = (
            candidate_metrics.step_label
            or diff_table.get_step_label(composite_key)
        )
        
        # Get normalized metrics for the candidate
        candidate_dict = normalize_to_dict(candidate_metrics)
        
        # Show all metrics from candidate (baseline is None for all)
        # Prioritize showing execution.time and llm.usage metrics
        important_metrics = ["execution.time", "llm.usage.prompt-tokens", "llm.usage.completion-tokens", "llm.usage.total-tokens"]
        other_metrics = [m for m in sorted(candidate_dict.keys()) if m not in important_metrics]
        
        # Show important metrics first
        for metric_name in important_metrics + other_metrics:
            candidate_value = candidate_dict.get(metric_name)
            # Only show metrics that have values, or always show important metrics
            if candidate_value is not None or metric_name in important_metrics:
                row = DiffRow(
                    step=step_label,
                    metric=metric_name,
                    baseline_value=None,
                    candidate_value=candidate_value,
                    delta=None,
                    status=classification.status,
                    status_reason=classification.reason if metric_name == important_metrics[0] else None,
                )
                diff_table.add_row(row)

    return diff_table

