"""Builder to create multi-version DiffTable models."""

from typing import Dict, List, Set, Tuple
from regression_diff.parser import ParsedResult, parse_regression_artifact
from regression_diff.normalizer import normalize_result, normalize_to_dict, NormalizedMetrics
from regression_diff.models_multi import MultiVersionDiffTable, MultiVersionDiffRow
from regression_diff.config import Config
from collections import defaultdict


def build_multi_version_diff_table(
    group: str,
    versions: List[str],
    version_results: Dict[str, Dict[Tuple[str, str, str, str], ParsedResult]],
    config: Config,
) -> MultiVersionDiffTable:
    """
    Build a multi-version diff table with all versions as columns.
    
    Args:
        group: Group name
        versions: List of version identifiers in order
        version_results: Dictionary mapping version -> {composite_key -> ParsedResult}
        config: Configuration
        
    Returns:
        MultiVersionDiffTable instance
    """
    table = MultiVersionDiffTable(group=group, versions=versions)
    
    # Collect all composite keys across all versions
    all_keys: Set[Tuple[str, str, str, str]] = set()
    for version in versions:
        if version in version_results:
            all_keys.update(version_results[version].keys())
    
    # For each composite key, create rows for each metric
    for composite_key in sorted(all_keys):
        # Get normalized metrics for each version
        version_metrics: Dict[str, NormalizedMetrics] = {}
        for version in versions:
            if version in version_results and composite_key in version_results[version]:
                parsed_result = version_results[version][composite_key]
                version_metrics[version] = normalize_result(parsed_result)
        
        # Get step label from first available version
        step_label = None
        for version in versions:
            if version in version_metrics:
                step_label = version_metrics[version].step_label
                if step_label:
                    break
        
        if not step_label:
            step_label = table.get_step_label(composite_key)
        
        # Get all metric names that appear in any version
        all_metric_names: Set[str] = set()
        for version in versions:
            if version in version_metrics:
                metrics_dict = normalize_to_dict(version_metrics[version])
                all_metric_names.update(metrics_dict.keys())
        
        # Prioritize important metrics
        important_metrics = ["execution.time", "llm.usage.prompt-tokens", "llm.usage.completion-tokens", "llm.usage.total-tokens"]
        other_metrics = sorted([m for m in all_metric_names if m not in important_metrics])
        sorted_metrics = important_metrics + other_metrics
        
        # Create a row for each metric
        for metric_name in sorted_metrics:
            # Collect values for this metric across all versions
            version_values: Dict[str, Any] = {}
            has_value = False
            
            for version in versions:
                if version in version_metrics:
                    metrics_dict = normalize_to_dict(version_metrics[version])
                    value = metrics_dict.get(metric_name)
                    version_values[version] = value
                    if value is not None:
                        has_value = True
                else:
                    version_values[version] = None
            
            # Only create row if metric has at least one value, or it's an important metric
            if has_value or metric_name in important_metrics:
                # Check if this is a numeric metric
                is_numeric = False
                numeric_values = [v for v in version_values.values() if v is not None and isinstance(v, (int, float))]
                if len(numeric_values) > 0:
                    # Check if all non-None values are numeric
                    all_numeric = all(
                        v is None or isinstance(v, (int, float))
                        for v in version_values.values()
                    )
                    if all_numeric:
                        is_numeric = True
                
                # Calculate absolute and percent changes for numeric metrics
                absolute_changes = {}
                percent_changes = {}
                if is_numeric and len(versions) > 1:
                    # Calculate changes between adjacent versions
                    for i in range(len(versions) - 1):
                        prev_version = versions[i]
                        next_version = versions[i + 1]
                        prev_value = version_values.get(prev_version)
                        next_value = version_values.get(next_version)
                        
                        if prev_value is not None and next_value is not None:
                            try:
                                prev_num = float(prev_value)
                                next_num = float(next_value)
                                abs_change = next_num - prev_num
                                absolute_changes[f"{prev_version} → {next_version}"] = abs_change
                                
                                # Calculate percent change (handle division by zero)
                                if prev_num != 0:
                                    pct_change = (abs_change / abs(prev_num)) * 100
                                else:
                                    # If baseline is 0 and new value is not 0, percent change is undefined/infinite
                                    # We'll use a special marker or large number
                                    pct_change = float('inf') if next_num != 0 else 0.0
                                percent_changes[f"{prev_version} → {next_version}"] = pct_change
                            except (ValueError, TypeError):
                                # Not numeric after all, skip
                                pass
                
                # Determine overall status - for now, set to PASS if all versions have same value
                # More sophisticated status calculation can be added later
                status = "PASS"
                status_reason = None
                
                # Check if there are differences between versions
                values_list = [v for v in version_values.values() if v is not None]
                if len(values_list) > 1:
                    # Check for differences (handle unhashable types like lists)
                    first_value = values_list[0]
                    all_same = True
                    for val in values_list[1:]:
                        # Compare values - for lists/sets, convert to sorted tuple for comparison
                        if isinstance(first_value, (list, set, tuple)) and isinstance(val, (list, set, tuple)):
                            if tuple(sorted(first_value)) != tuple(sorted(val)):
                                all_same = False
                                break
                        elif first_value != val:
                            all_same = False
                            break
                    
                    if not all_same:
                        # There are differences
                        status = "WARN"
                        status_reason = "Values differ across versions"
                
                # Check for missing values (version appeared/disappeared)
                present_versions = [v for v, val in version_values.items() if val is not None]
                if len(present_versions) < len(versions):
                    status = "WARN"
                    if status_reason:
                        status_reason += "; Some versions missing"
                    else:
                        status_reason = "Some versions missing"
                
                row = MultiVersionDiffRow(
                    step=step_label,
                    metric=metric_name,
                    version_values=version_values,
                    absolute_changes=absolute_changes,
                    percent_changes=percent_changes,
                    is_numeric=is_numeric,
                    status=status,
                    status_reason=status_reason,
                )
                table.add_row(row)
    
    return table

