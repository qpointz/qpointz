"""Tests for parser module."""

import pytest
from pathlib import Path
from regression_diff.parser import parse_regression_artifact, ParsedResult

FIXTURES_DIR = Path(__file__).parent / "fixtures"


def test_parse_regression_artifact():
    """Test parsing a regression artifact."""
    group = "test"
    version = "1.0"
    json_files = [FIXTURES_DIR / "sample_baseline.json"]

    results = parse_regression_artifact(group, version, json_files)

    assert len(results) > 0
    assert isinstance(results, dict)

    # Check composite key structure
    for key, result in results.items():
        assert isinstance(key, tuple)
        assert len(key) == 4
        assert isinstance(result, ParsedResult)
        assert result.group == group
        assert result.scenario_name == "test-scenario"


def test_parse_missing_scenario_name():
    """Test parsing artifact with missing scenarioName."""
    invalid_json = {
        "results": []
    }

    import json
    import tempfile
    with tempfile.NamedTemporaryFile(mode='w', suffix='.json', delete=False) as f:
        json.dump(invalid_json, f)
        temp_path = Path(f.name)

    try:
        with pytest.raises(ValueError, match="Missing 'scenarioName'"):
            parse_regression_artifact("test", "1.0", [temp_path])
    finally:
        temp_path.unlink()

