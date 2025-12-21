"""Tests for normalizer module."""

import pytest
from regression_diff.parser import ParsedResult
from regression_diff.normalizer import normalize_result, NormalizedMetrics


def test_normalize_result():
    """Test normalizing a parsed result."""
    parsed_result = ParsedResult(
        group="test",
        scenario_name="test-scenario",
        action_key="ask",
        action_params={"value": "list clients"},
        result_data={
            "action": {"key": "ask", "params": {"value": "list clients"}},
            "outcome": {
                "status": "PASS",
                "data": {
                    "resultIntent": "get-data",
                    "sql": "SELECT * FROM CLIENTS",
                },
                "metrics": {
                    "execution.time": 1000,
                    "llm.usage.prompt-tokens": 1000,
                },
            },
        },
    )

    metrics = normalize_result(parsed_result)

    assert isinstance(metrics, NormalizedMetrics)
    assert metrics.intent == "get-data"
    assert metrics.has_sql is True
    assert metrics.sql == "SELECT * FROM CLIENTS"
    assert metrics.execution_time_ms == 1000
    assert metrics.llm_prompt_tokens == 1000
    assert metrics.step_label == "list clients"


def test_normalize_result_missing_fields():
    """Test normalizing result with missing fields."""
    parsed_result = ParsedResult(
        group="test",
        scenario_name="test-scenario",
        action_key="ask",
        action_params={},
        result_data={
            "action": {"key": "ask", "params": {}},
            "outcome": {"status": "PASS", "data": {}},
        },
    )

    metrics = normalize_result(parsed_result)

    assert metrics.intent is None
    assert metrics.has_sql is False
    assert metrics.execution_time_ms is None

