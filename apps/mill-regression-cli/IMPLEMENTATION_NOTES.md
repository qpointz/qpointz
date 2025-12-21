# Implementation Notes: Multi-Version Diff Table Builder

## Overview

The Regression Diff Table Builder is a CLI tool that compares regression test results across multiple versions from a tar.gz archive. It generates diff tables showing metrics, values, and changes between versions.

## Architecture

### Core Components

1. **Archive Extraction** (`regression_diff/archive.py`)
   - Extracts tar.gz archives
   - Discovers structure: `/<group>/<version>/<*.json>`
   - Supports extensible version selection via `VersionSelector` interface

2. **JSON Parsing** (`regression_diff/parser.py`)
   - Parses JSON regression test reports
   - Merges multiple JSON files per version
   - Creates composite keys: `(group, scenario_name, action_key, serialized_action_params)`
   - Returns `ParsedResult` objects with composite keys

3. **Normalization** (`regression_diff/normalizer.py`)
   - Flattens complex JSON structures into `NormalizedMetrics`
   - Extracts metrics: execution time, LLM tokens, data size, SQL shape, etc.
   - Provides `normalize_to_dict()` for comparison-friendly format

4. **Multi-Version Builder** (`regression_diff/builder_multi.py`)
   - Collects data from all versions
   - Creates unified table with all versions as columns
   - Calculates absolute and percent changes for numeric metrics
   - Handles missing values (None) across versions

5. **Formatters**
   - **Markdown** (`regression_diff/formatters/markdown_multi.py`): Human-readable tables
   - **CSV** (`regression_diff/formatters/csv_multi.py`): Spreadsheet-friendly format
   - Both formats include:
     - Version value columns
     - Absolute change columns (Δ abs)
     - Percent change columns (Δ %)

6. **CLI** (`regression_diff/cli.py`)
   - Command-line interface using Click
   - Supports: `--format`, `--output`, `--config`, `--versions`, `--group`
   - Outputs to stdout, single file, or directory

## Data Models

### MultiVersionDiffTable
```python
@dataclass
class MultiVersionDiffTable:
    group: str
    versions: List[str]  # Ordered list of versions
    rows: List[MultiVersionDiffRow]
    metadata: Dict[str, Any]
```

### MultiVersionDiffRow
```python
@dataclass
class MultiVersionDiffRow:
    step: str
    metric: str
    version_values: Dict[str, Any]  # version -> value
    absolute_changes: Dict[str, float]  # "v1 → v2" -> abs_change
    percent_changes: Dict[str, float]  # "v1 → v2" -> pct_change
    is_numeric: bool
    status: str  # PASS/WARN/FAIL
    status_reason: Optional[str]
```

## Key Features

### 1. Multi-Version Comparison
- Single table showing all versions side-by-side
- Each version has its own column
- Easy to compare values across versions at a glance

### 2. Numeric Metrics with Changes
- **Absolute Changes**: `next_value - prev_value`
- **Percent Changes**: `(abs_change / abs(prev_value)) * 100`
- Changes calculated only for numeric metrics (int/float)
- Changes shown only when both values exist (not None)

### 3. Version Selection
- Extensible `VersionSelector` interface
- Default: `AllVersionsSelector` (includes all versions)
- Future: Can add pattern-based, date-based, or tag-based selectors

### 4. Composite Key Matching
- Uses `(group, scenario_name, action_key, serialized_action_params)` as unique identifier
- Matches test steps across versions even if file structure differs
- Handles missing steps (appear/disappear between versions)

### 5. Metric Extraction
- **Important metrics always shown**: execution.time, llm.usage.* (even if None)
- **Other metrics**: Shown when they have values
- Supports: int, float, bool, string, list/set (for SQL shape features)

## Output Format Structure

### CSV Format
```
Group, Step, Metric, v1, v2, v3, v4, v1→v2 (abs), v1→v2 (%), v2→v3 (abs), v2→v3 (%), v3→v4 (abs), v3→v4 (%), Status, Reason
```

### Markdown Format
```
| Step | Metric | v1 | v2 | v3 | v4 | v1→v2<br/>Δ (abs) | v1→v2<br/>Δ (%) | ... | Status |
```

## Change Calculation Logic

### Absolute Change
```python
abs_change = next_value - prev_value
```

### Percent Change
```python
if prev_value != 0:
    pct_change = (abs_change / abs(prev_value)) * 100
else:
    # Handle division by zero
    pct_change = float('inf') if next_value != 0 else 0.0
```

### Display Rules
- Changes shown only for numeric metrics (`is_numeric=True`)
- Changes shown only when both values exist (not None)
- Empty string ("") or "—" shown when change cannot be calculated

## Configuration

YAML configuration file (`regression_diff_config.yaml`) supports:
- Metric-specific comparison settings (thresholds, directions)
- Forbidden changes (intent changes, SQL join changes, etc.)
- Version selector type

Example:
```yaml
metrics:
  execution.time:
    direction: "less_is_better"
    threshold: 15000
    threshold_type: "absolute"
    warn_on_threshold_exceeded: true
```

## Usage Examples

### Basic usage
```bash
python -m regression_diff.cli test.tar.gz --format markdown
```

### Output to file
```bash
python -m regression_diff.cli test.tar.gz --format csv --output results.csv
```

### Specific versions
```bash
python -m regression_diff.cli test.tar.gz --versions "0.5.0-dev:0.5.0-rc.6"
```

### With config
```bash
python -m regression_diff.cli test.tar.gz --config regression_diff_config.yaml
```

## Implementation Details

### Handling Missing Values
- When a step/metric doesn't exist in a version: shows "—" (Markdown) or "" (CSV)
- Status marked as "WARN" with reason "Some versions missing"
- Changes not calculated when either value is None

### Status Classification
- **PASS**: All versions have same value, or values within acceptable thresholds
- **WARN**: Values differ across versions, or some versions missing
- **FAIL**: Forbidden changes detected (e.g., intent change, unexpected SQL join)

### Composite Key Generation
- Groups related actions by scenario and action type
- Serializes action parameters to JSON string for stable matching
- Handles different action types: "ask", "verify", "reply"

## Future Enhancements

1. **Advanced Version Selectors**
   - Pattern-based: `--version-selector "regex:^0\.5\.*"`
   - Latest N versions: `--version-selector "latest-3"`
   - Date-based: `--version-selector "after:2024-01-01"`

2. **JSON Multi-Version Formatter**
   - Structured JSON output for programmatic consumption

3. **Enhanced Status Calculation**
   - Apply configuration thresholds to changes
   - More sophisticated PASS/WARN/FAIL logic

4. **Filtering Options**
   - Filter by step/scenario
   - Filter by metric type
   - Show only changed metrics

5. **Statistical Summaries**
   - Aggregate statistics across all steps
   - Trend analysis (improving/degrading metrics)

## File Structure

```
apps/mill-regression-cli/
├── regression_diff/
│   ├── __init__.py
│   ├── archive.py              # Archive extraction & structure discovery
│   ├── parser.py               # JSON parsing & composite key generation
│   ├── normalizer.py           # Flattening JSON to metrics
│   ├── matcher.py              # Matching results between versions (pairwise)
│   ├── builder_multi.py        # Multi-version table builder
│   ├── models_multi.py         # Multi-version data models
│   ├── comparator.py           # Metric comparison logic
│   ├── classifier.py           # Status classification
│   ├── config.py               # Configuration loading
│   ├── version_selector.py     # Version selection interface
│   ├── cli.py                  # Command-line interface
│   ├── metric_comparers/       # Modular metric comparison strategies
│   │   ├── base.py
│   │   ├── numeric.py
│   │   ├── boolean.py
│   │   ├── string.py
│   │   ├── set.py
│   │   └── registry.py
│   └── formatters/             # Output formatters
│       ├── base.py
│       ├── markdown_multi.py
│       └── csv_multi.py
├── regression_diff_config.yaml # Default configuration
└── pyproject.toml              # Project dependencies
```

## Testing

The tool has been tested with:
- Multiple versions (4 versions in test.tar.gz)
- Different scenarios (basic-regression, step-back-regression, trivial-scenario)
- Various metric types (numeric, boolean, string, lists)
- Missing values and version gaps

## Dependencies

- `click`: CLI framework
- `pyyaml`: Configuration file parsing
- Standard library: `tarfile`, `json`, `csv`, `pathlib`, `dataclasses`

## Notes

- The tool is designed to be CI/CD friendly (non-interactive, exit codes)
- Output formats are format-agnostic (single model, multiple formatters)
- Version selection is extensible via plugin-like interface
- Metric comparison is modular (different strategies for different types)

