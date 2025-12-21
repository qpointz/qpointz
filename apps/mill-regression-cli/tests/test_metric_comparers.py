"""Tests for metric comparers."""

import pytest
from regression_diff.metric_comparers.numeric import NumericComparer
from regression_diff.metric_comparers.boolean import BooleanComparer
from regression_diff.metric_comparers.string import StringComparer
from regression_diff.metric_comparers.base import MetricConfig


def test_numeric_comparer_less_is_better():
    """Test numeric comparer with less_is_better direction."""
    comparer = NumericComparer()
    config = MetricConfig(
        metric_name="execution.time",
        direction="less_is_better",
        threshold=1000.0,
        threshold_type="absolute",
    )

    # Improvement (decrease)
    result = comparer.compare(1000, 800, config)
    assert result.status == "PASS"
    assert result.delta == -200

    # Degradation (increase within threshold)
    result = comparer.compare(1000, 1100, config)
    assert result.status == "PASS"  # Within threshold

    # Degradation exceeding threshold
    config.warn_on_threshold_exceeded = True
    result = comparer.compare(1000, 2500, config)
    assert result.status == "WARN"
    assert result.delta == 1500


def test_boolean_comparer():
    """Test boolean comparer."""
    comparer = BooleanComparer()
    config = MetricConfig(metric_name="has.sql", direction="equal")

    # No change
    result = comparer.compare(True, True, config)
    assert result.status == "PASS"

    # Change
    result = comparer.compare(True, False, config)
    assert result.status == "WARN"


def test_string_comparer():
    """Test string comparer."""
    comparer = StringComparer()
    config = MetricConfig(metric_name="intent", direction="equal")

    # No change
    result = comparer.compare("get-data", "get-data", config)
    assert result.status == "PASS"

    # Change
    config.warn_on_threshold_exceeded = False
    result = comparer.compare("get-data", "get-chart", config)
    assert result.status == "FAIL"

