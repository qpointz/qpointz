"""Tests for multi-version diff table builder."""

import pytest
from regression_diff.builder_multi import build_multi_version_diff_table, _calculate_multi_version_status
from regression_diff.parser import ParsedResult
from regression_diff.config import Config
from regression_diff.models_multi import MultiVersionDiffTable, MultiVersionDiffRow


def test_build_multi_version_diff_table_basic():
    """Test building a basic multi-version diff table."""
    group = "test"
    versions = ["v1", "v2"]
    
    # Create sample parsed results
    version_results = {
        "v1": {
            ("test", "scenario1", "ask", '{"value": "test"}'): ParsedResult(
                group="test",
                scenario_name="scenario1",
                action_key="ask",
                action_params={"value": "test"},
                result_data={
                    "action": {"key": "ask", "params": {"value": "test"}},
                    "outcome": {
                        "status": "PASS",
                        "data": {},
                        "metrics": {
                            "execution.time": 1000,
                            "llm.usage.prompt-tokens": 500,
                        },
                    },
                },
            ),
        },
        "v2": {
            ("test", "scenario1", "ask", '{"value": "test"}'): ParsedResult(
                group="test",
                scenario_name="scenario1",
                action_key="ask",
                action_params={"value": "test"},
                result_data={
                    "action": {"key": "ask", "params": {"value": "test"}},
                    "outcome": {
                        "status": "PASS",
                        "data": {},
                        "metrics": {
                            "execution.time": 1200,
                            "llm.usage.prompt-tokens": 600,
                        },
                    },
                },
            ),
        },
    }
    
    config = Config()
    table = build_multi_version_diff_table(group, versions, version_results, config)
    
    assert isinstance(table, MultiVersionDiffTable)
    assert table.group == group
    assert table.versions == versions
    assert len(table.rows) > 0
    
    # Check that we have rows for important metrics
    execution_time_rows = [r for r in table.rows if r.metric == "execution.time"]
    assert len(execution_time_rows) > 0
    
    execution_row = execution_time_rows[0]
    assert execution_row.version_values["v1"] == 1000
    assert execution_row.version_values["v2"] == 1200
    assert execution_row.is_numeric is True
    assert "v1 → v2" in execution_row.absolute_changes
    assert execution_row.absolute_changes["v1 → v2"] == 200.0


def test_build_multi_version_diff_table_three_versions():
    """Test building a multi-version diff table with three versions."""
    group = "test"
    versions = ["v1", "v2", "v3"]
    
    version_results = {
        "v1": {
            ("test", "scenario1", "ask", '{"value": "test"}'): ParsedResult(
                group="test",
                scenario_name="scenario1",
                action_key="ask",
                action_params={"value": "test"},
                result_data={
                    "action": {"key": "ask", "params": {"value": "test"}},
                    "outcome": {
                        "status": "PASS",
                        "data": {},
                        "metrics": {"execution.time": 1000},
                    },
                },
            ),
        },
        "v2": {
            ("test", "scenario1", "ask", '{"value": "test"}'): ParsedResult(
                group="test",
                scenario_name="scenario1",
                action_key="ask",
                action_params={"value": "test"},
                result_data={
                    "action": {"key": "ask", "params": {"value": "test"}},
                    "outcome": {
                        "status": "PASS",
                        "data": {},
                        "metrics": {"execution.time": 1200},
                    },
                },
            ),
        },
        "v3": {
            ("test", "scenario1", "ask", '{"value": "test"}'): ParsedResult(
                group="test",
                scenario_name="scenario1",
                action_key="ask",
                action_params={"value": "test"},
                result_data={
                    "action": {"key": "ask", "params": {"value": "test"}},
                    "outcome": {
                        "status": "PASS",
                        "data": {},
                        "metrics": {"execution.time": 1100},
                    },
                },
            ),
        },
    }
    
    config = Config()
    table = build_multi_version_diff_table(group, versions, version_results, config)
    
    execution_time_rows = [r for r in table.rows if r.metric == "execution.time"]
    assert len(execution_time_rows) > 0
    
    execution_row = execution_time_rows[0]
    assert len(execution_row.absolute_changes) == 2  # v1→v2 and v2→v3
    assert "v1 → v2" in execution_row.absolute_changes
    assert "v2 → v3" in execution_row.absolute_changes
    assert execution_row.absolute_changes["v1 → v2"] == 200.0
    assert execution_row.absolute_changes["v2 → v3"] == -100.0


def test_build_multi_version_diff_table_missing_version():
    """Test building a multi-version diff table when a version is missing."""
    group = "test"
    versions = ["v1", "v2", "v3"]
    
    version_results = {
        "v1": {
            ("test", "scenario1", "ask", '{"value": "test"}'): ParsedResult(
                group="test",
                scenario_name="scenario1",
                action_key="ask",
                action_params={"value": "test"},
                result_data={
                    "action": {"key": "ask", "params": {"value": "test"}},
                    "outcome": {
                        "status": "PASS",
                        "data": {},
                        "metrics": {"execution.time": 1000},
                    },
                },
            ),
        },
        "v2": {
            ("test", "scenario1", "ask", '{"value": "test"}'): ParsedResult(
                group="test",
                scenario_name="scenario1",
                action_key="ask",
                action_params={"value": "test"},
                result_data={
                    "action": {"key": "ask", "params": {"value": "test"}},
                    "outcome": {
                        "status": "PASS",
                        "data": {},
                        "metrics": {"execution.time": 1200},
                    },
                },
            ),
        },
        # v3 is missing
    }
    
    config = Config()
    table = build_multi_version_diff_table(group, versions, version_results, config)
    
    execution_time_rows = [r for r in table.rows if r.metric == "execution.time"]
    assert len(execution_time_rows) > 0
    
    execution_row = execution_time_rows[0]
    assert execution_row.version_values["v1"] == 1000
    assert execution_row.version_values["v2"] == 1200
    assert execution_row.version_values["v3"] is None
    assert execution_row.status == "WARN"
    assert "Some versions missing" in (execution_row.status_reason or "")


def test_calculate_multi_version_status_equal_metric():
    """Test status calculation for 'equal' direction metric."""
    metric_name = "intent"
    version_values = {"v1": "get-data", "v2": "get-data", "v3": "get-chart"}
    versions = ["v1", "v2", "v3"]
    absolute_changes = {}
    is_numeric = False
    config = Config()
    
    status, reason = _calculate_multi_version_status(
        metric_name, version_values, versions, absolute_changes, is_numeric, config
    )
    
    # Intent change should be FAIL (warn_on_threshold_exceeded is False for intent)
    assert status == "FAIL"
    assert "Value changed" in (reason or "")


def test_calculate_multi_version_status_numeric_threshold():
    """Test status calculation for numeric metric with threshold."""
    metric_name = "execution.time"
    version_values = {"v1": 1000, "v2": 20000}  # Exceeds threshold of 15000
    versions = ["v1", "v2"]
    absolute_changes = {"v1 → v2": 19000}
    is_numeric = True
    config = Config()
    
    status, reason = _calculate_multi_version_status(
        metric_name, version_values, versions, absolute_changes, is_numeric, config
    )
    
    # Should be WARN (warn_on_threshold_exceeded is True for execution.time)
    assert status == "WARN"
    assert reason is not None
