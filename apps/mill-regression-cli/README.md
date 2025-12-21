# Regression Diff Table Builder

A Python CLI tool for comparing regression test run artifacts and generating human-readable diff tables showing what changed between versions.

## Features

- Extracts and analyzes tar.gz archives containing regression test results
- Merges multiple JSON files per version directory
- Matches results by composite key (group, scenario, action, params)
- Compares metrics with configurable thresholds and direction preferences
- Generates diff tables in Markdown, CSV, or JSON formats
- Extensible version selector for filtering versions
- Modular metric comparers with support for different metric types

## Installation

This project uses Poetry for dependency management.

```bash
cd apps/mill-regression-cli
poetry install
```

## Usage

### Basic Usage

```bash
poetry run regression-diff archive.tar.gz
```

### Options

- `ARCHIVE`: Path to tar.gz file (required)
- `--config PATH`: Path to YAML configuration file (optional)
- `--output PATH`: Output file or directory (default: stdout)
- `--format {markdown,csv,json}`: Output format (default: markdown)
- `--group GROUP`: Filter to specific group only (optional)
- `--versions baseline:candidate`: Specify version pair explicitly (optional)
- `--version-selector STRATEGY`: Version selector strategy (default: "all")

### Examples

```bash
# Compare all adjacent versions and output to stdout
poetry run regression-diff results.tar.gz

# Output to a file
poetry run regression-diff results.tar.gz --output diff.md

# Output to directory (one file per group/version-pair)
poetry run regression-diff results.tar.gz --output ./diffs/

# Use CSV format
poetry run regression-diff results.tar.gz --format csv --output diff.csv

# Compare specific version pair
poetry run regression-diff results.tar.gz --versions 0.5.0-dev:0.5.0-test

# Filter to specific group
poetry run regression-diff results.tar.gz --group ai

# Use custom configuration
poetry run regression-diff results.tar.gz --config custom_config.yaml
```

## Archive Structure

The tool expects tar.gz archives with the following structure:

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

### Markdown

Default format, produces Markdown tables:

```markdown
### ai — 0.5.0-dev → 0.5.0-test

| Step | Metric | Baseline | Candidate | Δ | Status |
|------|--------|----------|-----------|---|--------|
| list clients | execution.time | 1000 | 1200 | +200 | PASS |
```

### CSV

Machine-readable CSV format:

```csv
Step,Metric,Baseline,Candidate,Delta,Status
list clients,execution.time,1000,1200,+200,PASS
```

### JSON

Structured JSON output with full model representation:

```json
{
  "group": "ai",
  "baseline_version": "0.5.0-dev",
  "candidate_version": "0.5.0-test",
  "rows": [...]
}
```

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
4. **Matching**: Aligns results by composite key
5. **Comparison**: Uses modular metric comparers with configurable rules
6. **Classification**: Determines PASS/WARN/FAIL status
7. **Model Building**: Creates format-agnostic DiffTable model
8. **Formatting**: Renders model in desired output format

## License

Internal tool for QPointz project.

