# Excel Format

Reads Microsoft Excel files (.xlsx and .xls). Mill uses Apache POI to parse workbooks and extract sheet data as rows.

Excel is widely used for business data, reports, and data exchange. Mill can read specific sheets by name, index, or pattern, and supports header detection, formula evaluation, and blank-as-null handling.

---

## Configuration Reference

Specify `type: excel` on the reader and place format options under the `format` key.

```yaml
readers:
  - type: excel
    format:
      hasHeader: true
      sheetName: "Sheet1"
```

### Sheet reading settings

| Property     | Required | Default | Description                                                    |
|--------------|----------|---------|----------------------------------------------------------------|
| `hasHeader`  | no       | `true`  | Whether the first row contains column names.                   |

All columns are typed as nullable `STRING` in the schema. Cell values are extracted as their natural types during reading (numbers, booleans, dates, strings).

### Sheet selection

Sheet selection properties control which sheets in a workbook are read. When multiple sheets are selected, their rows are **concatenated into a single table** — all selected sheets must share the same column structure.

Selection properties are evaluated in priority order:

| Property              | Type          | Default  | Description                                                    |
|-----------------------|---------------|----------|----------------------------------------------------------------|
| `allSheets`           | bool          | `false`  | Select every sheet in the workbook.                            |
| `sheetPattern`        | string (regex)| —        | Select sheets whose name matches the regex.                    |
| `sheetName`           | string        | —        | Select a single sheet by exact name.                           |
| `sheetIndex`          | int           | —        | Select a single sheet by zero-based index.                     |
| `excludeSheets`       | list[string]  | —        | Exclude sheets by exact name (combined with any include rule). |
| `excludeSheetPattern` | string (regex)| —        | Exclude sheets whose name matches the regex.                   |

**Priority:** `allSheets` > `sheetPattern` > `sheetName` > `sheetIndex` > first sheet (default).

**Exclude rules** are applied after include rules and can be combined with any include strategy. For example, `allSheets: true` with `excludeSheets: ["Summary"]` selects all sheets except "Summary".

If no sheet selection property is specified, only the first sheet is read.

---

## Examples

### Read first sheet (default)

```yaml
name: reports
storage:
  type: local
  rootPath: /data/reports
readers:
  - type: excel
    format:
      hasHeader: true
    table:
      mapping:
        type: regex
        pattern: ".*(?<table>[^/]+)\\.xlsx$"
```

### Read a specific sheet by name

```yaml
name: financials
storage:
  type: local
  rootPath: /data/finance
readers:
  - type: excel
    format:
      hasHeader: true
      sheetName: "Q4 Summary"
    table:
      mapping:
        type: directory
        depth: 1
```

### Read a specific sheet by index

```yaml
name: inventory
storage:
  type: local
  rootPath: /data/inventory
readers:
  - type: excel
    format:
      hasHeader: true
      sheetIndex: 2
    table:
      mapping:
        type: regex
        pattern: ".*(?<table>[^/]+)\\.xlsx$"
```

### Combine monthly sheets into one table

A common pattern: each workbook has one sheet per month (e.g. "Jan", "Feb", ...) plus a "Summary" sheet with a different structure. Use `allSheets` with `excludeSheets` to read only the month tabs and merge them.

```yaml
name: monthly-reports
storage:
  type: local
  rootPath: /data/monthly
readers:
  - type: excel
    format:
      hasHeader: true
      allSheets: true
      excludeSheets:
        - "Summary"
    table:
      mapping:
        type: regex
        pattern: ".*(?<table>[^/]+)\\.xlsx$"
```

All rows from "Jan", "Feb", "Mar", etc. are concatenated into a single table. The "Summary" sheet is excluded because it has a different column structure.

### Select sheets by pattern

Use `sheetPattern` to match sheet names with a regex. This is useful when sheet names follow a naming convention.

```yaml
name: quarterly-data
storage:
  type: local
  rootPath: /data/quarterly
readers:
  - type: excel
    format:
      hasHeader: true
      sheetPattern: "2024_Q[1-4]"
    table:
      mapping:
        type: regex
        pattern: ".*(?<table>[^/]+)\\.xlsx$"
```

This selects sheets named "2024_Q1", "2024_Q2", "2024_Q3", "2024_Q4" and combines their rows.

### Pattern with exclusion

```yaml
name: regional-sales
storage:
  type: local
  rootPath: /data/sales
readers:
  - type: excel
    format:
      hasHeader: true
      sheetPattern: ".*"
      excludeSheetPattern: "(Summary|Metadata)"
    table:
      mapping:
        type: regex
        pattern: ".*(?<table>[^/]+)\\.xlsx$"
```

Selects all sheets except those named "Summary" or "Metadata".

### Glob — all Excel files as one table

```yaml
name: report-archive
storage:
  type: local
  rootPath: /data/reports
readers:
  - type: excel
    format:
      hasHeader: true
    table:
      mapping:
        type: glob
        pattern: "**/*.{xlsx,xls}"
        table: all_reports
```

All Excel files (both `.xlsx` and `.xls`) under `/data/reports` are combined into the `all_reports` table.
