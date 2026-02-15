"""Root conftest — register custom pytest markers and shared fixtures."""
from __future__ import annotations

import pytest


def pytest_collection_modifyitems(config: pytest.Config, items: list[pytest.Item]) -> None:
    """Auto-apply ``unit`` marker to tests under ``tests/unit/`` that have no marker."""
    for item in items:
        if "unit" in str(item.fspath) and not any(
            m.name in ("unit", "integration") for m in item.iter_markers()
        ):
            item.add_marker(pytest.mark.unit)
        elif "integration" in str(item.fspath) and not any(
            m.name in ("unit", "integration") for m in item.iter_markers()
        ):
            item.add_marker(pytest.mark.integration)
