"""JSON artifact parser and merger."""

import json
from pathlib import Path
from typing import Dict, List, Any, Tuple
from collections import defaultdict


class ParsedResult:
    """Represents a parsed result with composite key."""

    def __init__(
        self,
        group: str,
        scenario_name: str,
        action_key: str,
        action_params: Dict[str, Any],
        result_data: Dict[str, Any],
    ):
        """Initialize parsed result."""
        self.group = group
        self.scenario_name = scenario_name
        self.action_key = action_key
        self.action_params = action_params
        self.result_data = result_data
        self.composite_key = self._create_composite_key()

    def _create_composite_key(self) -> Tuple[str, str, str, str]:
        """Create composite key for matching."""
        # Serialize action_params to normalized JSON string
        params_str = json.dumps(self.action_params, sort_keys=True, separators=(",", ":"))
        return (self.group, self.scenario_name, self.action_key, params_str)


def normalize_json(obj: Any) -> str:
    """Normalize JSON object to string for consistent comparison."""
    return json.dumps(obj, sort_keys=True, separators=(",", ":"))


def load_json_file(file_path: Path) -> Dict[str, Any]:
    """Load and parse JSON file."""
    try:
        with open(file_path, "r", encoding="utf-8") as f:
            return json.load(f)
    except json.JSONDecodeError as e:
        raise ValueError(f"Invalid JSON in {file_path}: {e}") from e
    except Exception as e:
        raise ValueError(f"Failed to read {file_path}: {e}") from e


def parse_regression_artifact(
    group: str, version: str, json_files: List[Path]
) -> Dict[Tuple[str, str, str, str], ParsedResult]:
    """
    Parse and merge all JSON files for a version.

    Args:
        group: Group name (extracted from path)
        version: Version identifier
        json_files: List of JSON file paths for this version

    Returns:
        Dictionary mapping composite_key -> ParsedResult

    Raises:
        ValueError: If JSON structure is invalid
    """
    results: Dict[Tuple[str, str, str, str], ParsedResult] = {}

    for json_file in json_files:
        data = load_json_file(json_file)

        # Validate required fields
        if "scenarioName" not in data:
            raise ValueError(
                f"Missing 'scenarioName' field in {json_file}"
            )

        scenario_name = data["scenarioName"]

        if "results" not in data:
            raise ValueError(
                f"Missing 'results' field in {json_file}"
            )

        # Process each result
        for result in data.get("results", []):
            if "action" not in result:
                continue

            action = result.get("action", {})
            action_key = action.get("key", "")
            action_params = action.get("params", {})

            # Create parsed result
            parsed_result = ParsedResult(
                group=group,
                scenario_name=scenario_name,
                action_key=action_key,
                action_params=action_params,
                result_data=result,
            )

            # Use composite key for mapping
            # If duplicate key exists, later file overwrites (could be improved)
            results[parsed_result.composite_key] = parsed_result

    return results

