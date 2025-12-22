"""Builder to create action-grouped report structure."""

import json
from typing import Dict, List, Tuple
from regression_diff.parser import ParsedResult
from regression_diff.models_action_grouped import ActionGroupedReport, ActionEntry, VersionAction


def build_action_grouped_report(
    group: str,
    versions: List[str],
    version_results: Dict[str, Dict[Tuple[str, str, str, str], ParsedResult]],
) -> ActionGroupedReport:
    """
    Build an action-grouped report from version results.
    
    Args:
        group: Group name
        versions: List of version identifiers in order
        version_results: Dictionary mapping version -> {composite_key -> ParsedResult}
        
    Returns:
        ActionGroupedReport instance
    """
    report = ActionGroupedReport()
    
    # Collect all composite keys across all versions
    all_keys: set = set()
    for version in versions:
        if version in version_results:
            all_keys.update(version_results[version].keys())
    
    # For each composite key (action), create an entry
    for composite_key in sorted(all_keys):
        group_name, scenario_name, action_key, params_str = composite_key
        
        # Get the action params (parse from JSON string)
        try:
            action_params = json.loads(params_str)
        except (json.JSONDecodeError, TypeError):
            action_params = {}
        
        # Create the top-level action object
        action_obj = {
            "scenario": scenario_name,
            "group": group_name,
            "key": action_key,
            "params": action_params,
        }
        
        # Collect all versions where this action appears
        version_actions: List[VersionAction] = []
        for version in versions:
            if version in version_results and composite_key in version_results[version]:
                parsed_result = version_results[version][composite_key]
                # Use the full result_data as the action object
                version_action = VersionAction(
                    version=version,
                    action=parsed_result.result_data,
                )
                version_actions.append(version_action)
        
        # Create entry for this action
        entry = ActionEntry(
            action=action_obj,
            versions=version_actions,
        )
        report.add_entry(entry)
    
    return report
