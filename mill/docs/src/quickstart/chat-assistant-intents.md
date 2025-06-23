# Chat Assistant Documentation

This document describes the core capabilities of the AI-powered chat assistant. Each **intent** below represents a supported function the assistant can perform based on natural language input.

## Supported Intents

### `get_data`
**Purpose:**  
Retrieve tabular data by expressing filters, conditions, or search criteria in plain language.

**Examples:**  
- "Show all clients in Switzerland."  
- "List products with inventory less than 10."  
- "Give me all orders placed last month."

**Returns:**  
SQL query and tabular results.

### `get_chart`
**Purpose:**  
Request data visualized as a chart. The assistant will choose an appropriate chart type (e.g. bar, pie, line) or use a specified one.

**Examples:**  
- "Show a bar chart of monthly revenue."  
- "Plot the number of users per region."  
- "Trend of sales over the past 12 months."

**Returns:**  
SQL query and chart configuration (e.g., ECharts or VegaLite JSON).

### `explain`
**Purpose:**  
Request a natural-language explanation for a table, query, or result. Also used when the user asks, “Describe what this means.”

**Examples:**  
- "Explain the purpose of the `orders` table."  
- "Describe the query we ran before."  
- "What does this chart show?"

**Returns:**  
Human-readable summary or breakdown of the schema or result.

### `constraint_check` (Planned)
**Purpose:**  
Identify data issues based on quality rules or constraints (e.g., nulls, value ranges, referential integrity).

**Examples:**  
- "Check if any orders are missing a shipping date."  
- "Are there any products without a category?"

**Returns:**  
SQL query and violations (if any).  
**_Currently planned. Not yet active._**

### `metadata_lookup` (Planned)
**Purpose:**  
Explore schema elements — such as tables, columns, and data types.

**Examples:**  
- "What tables are available?"  
- "What columns are in the `clients` table?"  
- "What does the `status` column mean?"

**Returns:**  
Schema metadata or table descriptions.  
**_Currently planned. Not yet active._**

### `enrich_model` (Planned)
**Purpose:**  
Let users provide **new metadata**, **assumptions**, or **domain rules** to help the assistant improve future query understanding.

**Examples:**  
- "Status = 'CHURNED' means the client has been inactive for 90+ days."  
- "A 'high-value client' is someone with > 10 orders and $5000 spent."

**Returns:**  
No SQL. Updates internal model with concept definitions.  
**_Currently planned. Not yet active._**

### `unsupported`
**Purpose:**  
Fallback for unsupported queries. Used when the assistant cannot map a request to a known function.

**Examples:**  
- "Book a meeting next week."  
- "Email this chart to my manager."

**Returns:**  
Friendly response indicating that the request isn’t supported, along with an optional `plannedIntent`.

## Future Intent Hints (`plannedIntent`)

If the assistant receives a request it can’t yet fulfill, it may return a `plannedIntent` to suggest future support:

- `compare` — Compare groups or metrics (e.g. A vs B)
- `trend_analysis` — Detect or display trends over time
- `refine_query` — Apply follow-ups or filters to an earlier query
- `summarize` — Generate plain-language summaries of results
- `export_data` — Request to export results (e.g., to CSV, Excel, etc.)

## Naming Conventions

Each response includes a **`queryName`**: a short, lowercase, file-safe name summarizing the request.  
Example:  
- Question: “Clients in Korea” → `clients_in_korea`

## Schema Strategy

The assistant also recommends how schema should be supplied to downstream models:

- `schemaScope`: `"full"`, `"partial"`, or `"none"` — how much schema is needed
- `schemaStrategy`: `"full_in_system_prompt"`, `"partial_runtime_injection"`, or `"none"` — how schema should be injected