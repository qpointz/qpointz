# Regression Diff CLI Tool

A Python CLI tool for comparing regression test run artifacts across multiple versions and generating reports in various formats.

## Features

- **Multi-version comparison**: Compare test results across multiple versions in a single table
- **Action-grouped JSON reports**: Generate structured JSON reports grouped by action
- **Metrics reports**: Extract and report specific metrics in CSV format with hierarchical path support
- **Flexible output formats**: Markdown, CSV, and JSON formats
- **Configurable thresholds**: Customize metric comparison rules via YAML configuration
- **Version filtering**: Select specific versions or include all versions
- **Extensible architecture**: Easy to add new metrics, formatters, and report types

## Installation

This project uses Poetry for dependency management.

```bash
cd apps/mill-regression-cli
poetry install
```

## Commands

The tool provides two main commands:

### `diff` - Compare Versions

Compare regression test run artifacts and generate diff tables.

```bash
poetry run regression-diff diff archive.tar.gz
```

**Options:**
- `ARCHIVE`: Path to tar.gz file (required)
- `--config PATH`: Path to YAML configuration file (optional)
- `--output PATH`: Output file or directory (default: stdout)
- `--format {markdown,csv,json}`: Output format (default: markdown)
- `--group GROUP`: Filter to specific group only (optional)
- `--versions baseline:candidate`: Specify version pair (optional, for backwards compatibility)
- `--version-selector STRATEGY`: Version selector strategy (default: "all")

**Examples:**

```bash
# Compare all versions and output to stdout
poetry run regression-diff diff results.tar.gz

# Output to a file
poetry run regression-diff diff results.tar.gz --output diff.md

# Use JSON format (action-grouped structure)
poetry run regression-diff diff results.tar.gz --format json --output report.json

# Compare specific version pair
poetry run regression-diff diff results.tar.gz --versions 0.5.0-dev:0.5.0-test

# Filter to specific group
poetry run regression-diff diff results.tar.gz --group ai
```

### `report` - Generate Metrics Report

Generate CSV reports from action-grouped JSON data with configurable metrics.

```bash
poetry run regression-diff report test_output.json \
  --metrics "action.success,action.outcome.metrics.execution.time" \
  --versions "v1,v2" \
  --format csv \
  --output metrics_report.csv
```

**Options:**
- `JSON_FILE`: Path to JSON file containing action-grouped data (required)
- `--metrics METRICS`: Comma-separated list of hierarchical metric paths (required)
- `--versions VERSIONS`: Optional comma-separated list of versions to include (if omitted, includes all versions)
- `--format {csv}`: Output format (default: csv)
- `--output PATH`: Output file path (default: stdout)

**Metric Paths:**

Metrics use hierarchical dot-notation paths starting from the action root:

- `action.success` - Action success status
- `action.outcome.status` - Outcome status (PASS/FAIL/ERROR)
- `action.outcome.metrics.execution.time` - Execution time in milliseconds
- `action.outcome.metrics.llm.usage.prompt-tokens` - LLM prompt tokens
- `action.outcome.metrics.llm.usage.total-tokens` - LLM total tokens
- `action.outcome.metrics.llm.usage.completion-tokens` - LLM completion tokens

**Examples:**

```bash
# Generate report with all versions
poetry run regression-diff report test_output.json \
  --metrics "action.success,action.outcome.metrics.execution.time,action.outcome.metrics.llm.usage.total-tokens" \
  --output report.csv

# Generate report with specific versions in order
poetry run regression-diff report test_output.json \
  --metrics "action.outcome.metrics.execution.time" \
  --versions "0.5.0-rc.6,0.5.0-test-regression-scenario" \
  --output execution_times.csv

# Output to stdout
poetry run regression-diff report test_output.json \
  --metrics "action.success,action.outcome.metrics.execution.time"
```

**CSV Output Format:**

The CSV report has the following structure:

```csv
group,scenario,action,metric,v1,v2,...,vN
ai,basic-regression,ask,action.success,false,true
ai,basic-regression,ask,action.outcome.metrics.execution.time,1329,35642
ai,basic-regression,ask,action.outcome.metrics.llm.usage.prompt-tokens,,4172
```

- One row per metric per action
- Columns: group, scenario, action, metric, followed by version columns
- Empty values shown as empty strings when metrics don't exist in a version

## Archive Structure

The `diff` command expects tar.gz archives with the following structure:

```
reports/
└── <group>/
    └── <version>/
        ├── file1.json
        ├── file2.json
        └── ...
```

Example:
```
reports/
└── ai/
    ├── 0.5.0-dev/
    │   ├── scenario1.json
    │   └── scenario2.json
    └── 0.5.0-test/
        ├── scenario1.json
        └── scenario2.json
```

## JSON Report Structure

When using `--format json` with the `diff` command, the output is an action-grouped structure:

```json
[
  {
    "action": {
      "scenario": "basic-regression",
      "group": "ai",
      "key": "ask",
      "params": {"value": "list clients"}
    },
    "versions": [
      {
        "version": "0.5.0-rc.6",
        "action": {<full result_data>}
      },
      {
        "version": "0.5.0-test-regression-scenario",
        "action": {<full result_data>}
      }
    ]
  }
]
```

This JSON structure can then be used with the `report` command to generate metrics reports.

## Configuration

Create a YAML configuration file to customize metric thresholds and comparison rules:

```yaml
metrics:
  llm.usage.prompt-tokens:
    direction: "less_is_better"
    threshold: 5000
    threshold_type: "absolute"
    warn_on_threshold_exceeded: true

  execution.time:
    direction: "less_is_better"
    threshold: 15000
    threshold_type: "absolute"
    warn_on_threshold_exceeded: true

  intent:
    direction: "equal"
    warn_on_threshold_exceeded: false  # Intent change = FAIL
```

### Metric Configuration Options

- `direction`: Comparison direction
  - `"less_is_better"`: Negative delta is improvement (e.g., execution time)
  - `"more_is_better"`: Positive delta is improvement (e.g., throughput)
  - `"equal"`: Values must match exactly (e.g., intent)
  - `"any"`: No direction preference

- `threshold`: Threshold value for significant changes
- `threshold_type`: `"absolute"` or `"relative"` (percentage)
- `warn_on_threshold_exceeded`: If `true`, threshold violations are WARN; if `false`, they are FAIL

## Output Formats

### Markdown (Default)

Human-readable Markdown tables with version columns and change calculations:

```markdown
### ai — Multi-Version Comparison

| Step | Metric | v1 | v2 | v3 | v1→v2<br/>Δ (abs) | v1→v2<br/>Δ (%) | Status |
|------|--------|----|----|----|-------------------|----------------|--------|
| list clients | execution.time | 1000 | 1200 | 1100 | 200.00 | 20.00% | PASS |
```

### CSV

Machine-readable CSV format with version columns:

```csv
Group,Step,Metric,v1,v2,v3,v1→v2 (abs),v1→v2 (%),Status
ai,list clients,execution.time,1000,1200,1100,200.00,20.00%,PASS
```

### JSON

Structured JSON output. For `diff` command, uses action-grouped structure. For multi-version tables, includes full model representation.

## Workflow Example

1. **Generate action-grouped JSON from archive:**
   ```bash
   poetry run regression-diff diff archive.tar.gz --format json --output test_output.json
   ```

2. **Generate metrics report from JSON:**
   ```bash
   poetry run regression-diff report test_output.json \
     --metrics "action.success,action.outcome.metrics.execution.time,action.outcome.metrics.llm.usage.total-tokens" \
     --versions "0.5.0-rc.6,0.5.0-test-regression-scenario" \
     --output metrics_report.csv
   ```

3. **Analyze the CSV report** in Excel, Google Sheets, or other tools.

## Development

### Running Tests

```bash
poetry run pytest
```

### Code Formatting

```bash
poetry run black regression_diff/
```

### Type Checking

```bash
poetry run mypy regression_diff/
```

## Architecture

The tool follows a modular architecture:

1. **Archive Extraction**: Extracts tar.gz and discovers structure
2. **Parsing**: Merges JSON files per version and creates composite keys
3. **Normalization**: Flattens JSON into comparable metrics
4. **Multi-Version Building**: Creates unified tables with all versions as columns
5. **Action Grouping**: Groups results by action for JSON output
6. **Metrics Extraction**: Extracts specific metrics using hierarchical paths
7. **Formatting**: Renders data in desired output format (Markdown, CSV, JSON)

## License

Internal tool for QPointz project.
