# AIMILL — Step-Back Reasoning + Scenario-Aware NL→SQL  
## Multi-Step Workflows, Comparison Tasks, and Analytical Pipelines

This document defines how Step-Back Reasoning extends AIMILL beyond single-query SQL generation into *multi-step analytical scenarios*, such as comparisons, correlations, trends, segment analysis, and custom workflows.

---

# 1. Purpose of Scenario-Aware NL→SQL

Users often request tasks that:

- require multiple SQL queries  
- involve sequential execution  
- need post-processing logic  
- require comparing results  
- depend on intermediate data transformations  

Example:

> “Compare user activity between Q3 and Q4.”

This cannot be solved by a single SQL query.

Scenario-aware NL→SQL interprets such requests as **workflows** rather than queries.

---

# 2. Why Step-Back Is Crucial for Scenario Planning

Step-Back Reasoning enables the system to:

1. Recognize that the user’s request is *not* a single-query task.  
2. Identify the scenario type (comparison, correlation, trend, segmentation).  
3. Break down the request into structured steps.  
4. Detect what inputs or definitions are missing.  
5. Ask clarifying questions before creating the plan.  
6. Produce a stable, interpretable multi-step execution graph.

Step-Back is the “global understanding” phase needed for planning.

---

# 3. Scenario Detection via Step-Back

A Scenario is detected if the Step-Back layer identifies:

- references to multiple datasets  
- temporal contrasts (e.g., “before vs after”)  
- comparisons of metrics  
- multi-part instructions  
- requests for relationships between outputs  

Example Step-Back output:

```json
"step-back": {
  "abstract-task": "The user wants to compare two time periods.",
  "core-concepts": ["user_activity", "quarter"],
  "required-relations": ["events -> users"],
  "ambiguities": ["definition of activity", "specific date field"],
  "scenario-type": "comparison",
  "needs-clarification": true
}
```

---

# 4. Clarification for Scenarios

Clarification questions are required before planning:

- Which metric defines activity?
- What exact time ranges correspond to “Q3” and “Q4”?
- Should comparison be row-level, aggregated, or both?

After user answers → scenario planning begins.

---

# 5. Scenario Planning Layer

Once ambiguities are resolved, the model constructs:

```json
"scenario": {
  "type": "comparison",
  "steps": [
    {
      "id": "A",
      "goal": "Retrieve dataset for period Q3",
      "intent": "get_data",
      "parameters": { "period": "Q3" }
    },
    {
      "id": "B",
      "goal": "Retrieve dataset for period Q4",
      "intent": "get_data",
      "parameters": { "period": "Q4" }
    },
    {
      "id": "C",
      "goal": "Compare results A and B",
      "intent": "compare",
      "method": "row_count, column_diff, metric_diff"
    }
  ]
}
```

This defines a fully deterministic workflow.

---

# 6. Supported Scenario Types

| Scenario Type | Description |
|---------------|-------------|
| **comparison** | Compare two datasets or time periods |
| **correlation** | Compute correlation between metrics or datasets |
| **trend** | Retrieve sequential data and evaluate temporal patterns |
| **segmentation** | Split data into groups and analyze differences |
| **pipeline** | Arbitrary sequence of dependent SQL and logic steps |

---

# 7. Multi-Step Execution Engine (FSM)

Execution flow:

```
User Query
  ↓
Step-Back Reasoning
  ↓
Clarification (loop until resolved)
  ↓
Scenario Planning
  ↓
For each scenario step:
    - Reasoning
    - SQL Generation
    - Execution
    - Store Outputs
  ↓
Post-Processing (comparison / correlation / trend analysis)
  ↓
Final Explanation
```

Each step can depend on previous steps’ outputs.

---

# 8. Comparison Logic Examples

After retrieving results for steps A and B, the model performs:

### 8.1 Row Count Comparison  
Difference in size.

### 8.2 Column Schema Comparison  
Missing columns, mismatched types.

### 8.3 Metric-Level Comparison  
Differences in aggregated values.

### 8.4 Row-Level Comparison  
Intersection, symmetric difference, key-based diff.

Output may be:

```json
"comparison-result": {
  "row-diff": "-18% in Q4",
  "missing-columns": [],
  "metric-diff": { "total_revenue": "-12%" },
  "summary": "User activity decreased in Q4."
}
```

---

# 9. Metadata Evolution Through Scenarios

Scenario reasoning exposes deeper gaps in metadata:

- undefined metrics (“activity”, “engagement”, “value”)
- missing business rules
- unclear semantics of time windows
- missing join keys for scenario coupling
- inconsistent naming conventions

These can be stored as:

```json
"metadata-gaps": [
  "Definition of 'active user' needed for comparison scenarios",
  "Missing canonical time dimension",
  "Metric 'engagement_score' lacks documentation"
]
```

This enables long-term iterative semantic improvement across sessions.

---

# 10. Benefits of Scenario-Aware NL→SQL

### ✔ Supports true analytical workflows  
### ✔ Handles multi-step queries  
### ✔ Performs comparisons, trends, correlations  
### ✔ Clarifies complex requests before planning  
### ✔ Produces interpretable execution graphs  
### ✔ Enables post-processing and reasoning on query results  
### ✔ Evolves semantic metadata organically  
### ✔ Transforms NL→SQL into a full analytical agent

---

# 11. Summary

Step-Back reasoning provides conceptual clarity and ambiguity detection.  
Scenario-aware NL→SQL extends this foundation to support multi-step analytical tasks.  
Together, they form a unified architecture where AIMILL becomes:

- a planner  
- a reasoning engine  
- a metadata improver  
- a workflow orchestrator  
- a semantic analysis agent  

This unlocks advanced NL→SQL use cases far beyond single-query retrieval.

---

# End of Document
