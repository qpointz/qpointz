"""Tests for multi-version formatters."""

import json
import pytest
from regression_diff.formatters.json_multi import JSONMultiVersionFormatter
from regression_diff.formatters.markdown_multi import MarkdownMultiVersionFormatter
from regression_diff.formatters.csv_multi import CSVMultiVersionFormatter
from regression_diff.models_multi import MultiVersionDiffTable, MultiVersionDiffRow


def test_json_multi_version_formatter():
    """Test JSON multi-version formatter."""
    formatter = JSONMultiVersionFormatter()
    
    table = MultiVersionDiffTable(
        group="test",
        versions=["v1", "v2"],
        rows=[
            MultiVersionDiffRow(
                step="test step",
                metric="execution.time",
                version_values={"v1": 1000, "v2": 1200},
                absolute_changes={"v1 → v2": 200.0},
                percent_changes={"v1 → v2": 20.0},
                is_numeric=True,
                status="PASS",
            )
        ],
    )
    
    output = formatter.format(table)
    assert isinstance(output, str)
    
    # Parse JSON to verify structure
    data = json.loads(output)
    assert data["group"] == "test"
    assert data["versions"] == ["v1", "v2"]
    assert len(data["rows"]) == 1
    
    row = data["rows"][0]
    assert row["step"] == "test step"
    assert row["metric"] == "execution.time"
    assert row["version_values"]["v1"] == 1000
    assert row["version_values"]["v2"] == 1200
    assert row["absolute_changes"]["v1 → v2"] == 200.0
    assert row["percent_changes"]["v1 → v2"] == 20.0
    assert row["is_numeric"] is True
    assert row["status"] == "PASS"


def test_json_multi_version_formatter_infinity():
    """Test JSON formatter handles infinity values."""
    formatter = JSONMultiVersionFormatter()
    
    table = MultiVersionDiffTable(
        group="test",
        versions=["v1", "v2"],
        rows=[
            MultiVersionDiffRow(
                step="test step",
                metric="execution.time",
                version_values={"v1": 0, "v2": 100},
                absolute_changes={"v1 → v2": 100.0},
                percent_changes={"v1 → v2": float('inf')},
                is_numeric=True,
                status="PASS",
            )
        ],
    )
    
    output = formatter.format(table)
    data = json.loads(output)
    
    row = data["rows"][0]
    assert row["percent_changes"]["v1 → v2"] == "inf"


def test_markdown_multi_version_formatter():
    """Test Markdown multi-version formatter."""
    formatter = MarkdownMultiVersionFormatter()
    
    table = MultiVersionDiffTable(
        group="test",
        versions=["v1", "v2"],
        rows=[
            MultiVersionDiffRow(
                step="test step",
                metric="execution.time",
                version_values={"v1": 1000, "v2": 1200},
                absolute_changes={"v1 → v2": 200.0},
                percent_changes={"v1 → v2": 20.0},
                is_numeric=True,
                status="PASS",
            )
        ],
    )
    
    output = formatter.format(table)
    assert isinstance(output, str)
    assert "test" in output
    assert "test step" in output
    assert "execution.time" in output
    assert "1000" in output
    assert "1200" in output
    assert "200.00" in output
    assert "20.00%" in output


def test_csv_multi_version_formatter():
    """Test CSV multi-version formatter."""
    formatter = CSVMultiVersionFormatter()
    
    table = MultiVersionDiffTable(
        group="test",
        versions=["v1", "v2"],
        rows=[
            MultiVersionDiffRow(
                step="test step",
                metric="execution.time",
                version_values={"v1": 1000, "v2": 1200},
                absolute_changes={"v1 → v2": 200.0},
                percent_changes={"v1 → v2": 20.0},
                is_numeric=True,
                status="PASS",
                status_reason="Test reason",
            )
        ],
    )
    
    output = formatter.format(table)
    assert isinstance(output, str)
    lines = output.strip().split("\n")
    assert len(lines) >= 2  # Header + at least one data row
    
    # Check header
    assert "Group" in lines[0]
    assert "Step" in lines[0]
    assert "Metric" in lines[0]
    assert "v1" in lines[0]
    assert "v2" in lines[0]
    
    # Check data row
    assert "test" in lines[1]
    assert "test step" in lines[1]
    assert "execution.time" in lines[1]
    assert "1000" in lines[1]
    assert "1200" in lines[1]
