SHELL := /bin/bash

MILL_PY_DIR := mill-py

.PHONY: help codegen test test-integration install uninstall build \
	publish-test publish clean

help:
	@echo "mill-py development targets"
	@echo "==========================="
	@echo ""
	@echo "  make codegen          Regenerate proto stubs (grpcio-tools)"
	@echo "  make test             Run unit tests with coverage"
	@echo "  make test-integration Run integration tests (requires running Mill service)"
	@echo "  make install          Install mill-py in editable mode (pip install -e)"
	@echo "  make uninstall        Uninstall mill-py from current environment"
	@echo "  make build            Build sdist + wheel (poetry build)"
	@echo "  make publish-test     Publish to TestPyPI (staging)"
	@echo "  make publish          Publish to PyPI (production release)"
	@echo "  make clean            Remove build artifacts, caches, coverage files"

# ---------------------------------------------------------------------------
# Proto codegen
# ---------------------------------------------------------------------------
codegen:
	@echo "Regenerating proto stubs..."
	cd $(MILL_PY_DIR) && python codegen.py

# ---------------------------------------------------------------------------
# Testing
# ---------------------------------------------------------------------------
test:
	@echo "Running unit tests with coverage..."
	cd $(MILL_PY_DIR) && poetry run pytest \
		--cov=mill --cov-report=term-missing \
		tests/unit/

test-integration:
	@echo "Running integration tests..."
	cd $(MILL_PY_DIR) && poetry run pytest -m integration -v

# ---------------------------------------------------------------------------
# Local install / uninstall
# ---------------------------------------------------------------------------
install:
	@echo "Installing mill-py in editable mode with all extras..."
	pip install -e "$(MILL_PY_DIR)[all]"

uninstall:
	@echo "Uninstalling mill-py..."
	pip uninstall -y mill-py

# ---------------------------------------------------------------------------
# Build
# ---------------------------------------------------------------------------
build:
	@echo "Building sdist + wheel..."
	cd $(MILL_PY_DIR) && poetry build

# ---------------------------------------------------------------------------
# Publish
# ---------------------------------------------------------------------------
publish-test:
	@echo "Publishing to TestPyPI..."
	cd $(MILL_PY_DIR) && poetry config repositories.testpypi https://test.pypi.org/legacy/
	cd $(MILL_PY_DIR) && poetry publish --repository testpypi

publish:
	@echo "Publishing to PyPI..."
	cd $(MILL_PY_DIR) && poetry publish

# ---------------------------------------------------------------------------
# Clean
# ---------------------------------------------------------------------------
clean:
	@echo "Cleaning build artifacts..."
	rm -rf $(MILL_PY_DIR)/dist/
	rm -rf $(MILL_PY_DIR)/.pytest_cache/
	rm -rf $(MILL_PY_DIR)/.coverage
	rm -rf $(MILL_PY_DIR)/coverage.xml
	rm -rf $(MILL_PY_DIR)/results.xml
	rm -rf $(MILL_PY_DIR)/integration-results.xml
	rm -rf $(MILL_PY_DIR)/htmlcov/
	find $(MILL_PY_DIR) -type d -name __pycache__ -exec rm -rf {} + 2>/dev/null || true
	@echo "Done."
