"""Normalizer to flatten JSON into comparable metrics model."""

from typing import Dict, Any, Optional, List, Set
from regression_diff.parser import ParsedResult


class NormalizedMetrics:
    """Flattened, normalized metrics from a result."""

    def __init__(self):
        """Initialize empty normalized metrics."""
        # Intent
        self.intent: Optional[str] = None

        # Presence flags
        self.has_sql: bool = False
        self.has_data: bool = False
        self.has_chart: bool = False
        self.has_enrichment: bool = False

        # SQL Shape (flattened)
        self.sql_shape_tables: List[str] = []
        self.sql_shape_has_join: bool = False
        self.sql_shape_has_where: bool = False
        self.sql_shape_has_aggregation: bool = False
        self.sql_shape_has_grouping: bool = False
        self.sql_shape_has_limit: bool = False
        self.sql_shape_select_arity: Optional[int] = None
        self.sql_shape_filter_columns: List[str] = []
        self.sql_shape_aggregation_functions: List[str] = []

        # Data metrics
        self.data_size: Optional[int] = None
        self.data_fields_count: Optional[int] = None
        self.data_container: Optional[str] = None

        # Performance metrics
        self.llm_prompt_tokens: Optional[int] = None
        self.llm_completion_tokens: Optional[int] = None
        self.llm_total_tokens: Optional[int] = None
        self.execution_time_ms: Optional[int] = None  # Converted to milliseconds

        # Verification status
        self.verify_status: Optional[str] = None

        # Raw SQL (if available)
        self.sql: Optional[str] = None

        # Step label (from ask action)
        self.step_label: Optional[str] = None


def normalize_result(parsed_result: ParsedResult) -> NormalizedMetrics:
    """
    Normalize a parsed result into flattened metrics.

    Args:
        parsed_result: Parsed result to normalize

    Returns:
        NormalizedMetrics instance
    """
    metrics = NormalizedMetrics()
    result_data = parsed_result.result_data

    # Extract step label from ask action
    if parsed_result.action_key == "ask":
        params = parsed_result.action_params
        if isinstance(params, dict) and "value" in params:
            metrics.step_label = str(params["value"])

    # Extract outcome data
    outcome = result_data.get("outcome", {})
    if outcome is None:
        outcome = {}
    outcome_data = outcome.get("data", {})
    if outcome_data is None:
        outcome_data = {}
    
    # Extract intent
    if outcome_data and "resultIntent" in outcome_data:
        metrics.intent = outcome_data["resultIntent"]
    elif outcome_data and "intent" in outcome_data:
        metrics.intent = outcome_data["intent"]
    
    # Extract presence flags
    if outcome_data:
        metrics.has_sql = "sql" in outcome_data
        metrics.has_data = "data" in outcome_data
        metrics.has_chart = "chart" in outcome_data
        metrics.has_enrichment = "enrichment" in outcome_data

    # Extract SQL
    if metrics.has_sql:
        metrics.sql = outcome_data.get("sql")

    # Extract SQL Shape from metrics
    metrics_obj = result_data.get("metrics", {})
    if metrics_obj is None:
        metrics_obj = {}
    sql_shape = metrics_obj.get("sql.shape", {})
    if sql_shape:
        metrics.sql_shape_tables = sql_shape.get("tables", [])
        metrics.sql_shape_has_join = sql_shape.get("hasJoin", False)
        metrics.sql_shape_has_where = sql_shape.get("hasWhere", False)
        metrics.sql_shape_has_aggregation = sql_shape.get("hasAggregation", False)
        metrics.sql_shape_has_grouping = sql_shape.get("hasGrouping", False)
        metrics.sql_shape_has_limit = sql_shape.get("hasLimit", False)
        metrics.sql_shape_select_arity = sql_shape.get("selectArity")
        metrics.sql_shape_filter_columns = sql_shape.get("filterColumns", [])
        metrics.sql_shape_aggregation_functions = sql_shape.get(
            "aggregationFunctions", []
        )

    # Extract data metrics
    if metrics.has_data and "data" in outcome_data:
        data = outcome_data["data"]
        if isinstance(data, dict):
            container = data.get("container", {})
            if isinstance(container, dict):
                container_data = container.get("data", [])
                if isinstance(container_data, list):
                    metrics.data_size = len(container_data)

                fields = container.get("fields", [])
                if isinstance(fields, list):
                    metrics.data_fields_count = len(fields)

                container_type = container.get("container-type")
                if container_type:
                    metrics.data_container = str(container_type)

    # Extract data metrics from metrics object (fallback)
    data_size_from_metrics = metrics_obj.get("data.size")
    if data_size_from_metrics is not None:
        metrics.data_size = int(data_size_from_metrics)

    data_fields = metrics_obj.get("data.fields", [])
    if isinstance(data_fields, list):
        metrics.data_fields_count = len(data_fields)

    data_container_from_metrics = metrics_obj.get("data.container")
    if data_container_from_metrics:
        metrics.data_container = str(data_container_from_metrics)

    # Extract LLM usage metrics
    llm_usage = metrics_obj.get("llm.usage", {})
    if isinstance(llm_usage, dict):
        metrics.llm_prompt_tokens = llm_usage.get("prompt-tokens")
        metrics.llm_completion_tokens = llm_usage.get("completion-tokens")
        metrics.llm_total_tokens = llm_usage.get("total-tokens")
    else:
        # Alternative format: llm.usage.prompt-tokens
        metrics.llm_prompt_tokens = metrics_obj.get("llm.usage.prompt-tokens")
        metrics.llm_completion_tokens = metrics_obj.get("llm.usage.completion-tokens")
        metrics.llm_total_tokens = metrics_obj.get("llm.usage.total-tokens")

    # Extract execution time (convert to milliseconds)
    execution_time = metrics_obj.get("execution.time")
    if execution_time is not None:
        # Assume execution.time is already in milliseconds
        metrics.execution_time_ms = int(execution_time)

    # Extract verification status
    if outcome:
        verify_status = outcome.get("status")
        if verify_status:
            metrics.verify_status = str(verify_status)

    return metrics


def normalize_to_dict(metrics: NormalizedMetrics) -> Dict[str, Any]:
    """Convert NormalizedMetrics to flat dictionary for comparison."""
    return {
        "intent": metrics.intent,
        "has.sql": metrics.has_sql,
        "has.data": metrics.has_data,
        "has.chart": metrics.has_chart,
        "has.enrichment": metrics.has_enrichment,
        "sqlShape.tables": sorted(metrics.sql_shape_tables),
        "sqlShape.hasJoin": metrics.sql_shape_has_join,
        "sqlShape.hasWhere": metrics.sql_shape_has_where,
        "sqlShape.hasAggregation": metrics.sql_shape_has_aggregation,
        "sqlShape.hasGrouping": metrics.sql_shape_has_grouping,
        "sqlShape.hasLimit": metrics.sql_shape_has_limit,
        "sqlShape.selectArity": metrics.sql_shape_select_arity,
        "sqlShape.filterColumns": sorted(metrics.sql_shape_filter_columns),
        "sqlShape.aggregationFunctions": sorted(
            metrics.sql_shape_aggregation_functions
        ),
        "data.size": metrics.data_size,
        "data.fields.count": metrics.data_fields_count,
        "data.container": metrics.data_container,
        "llm.usage.prompt-tokens": metrics.llm_prompt_tokens,
        "llm.usage.completion-tokens": metrics.llm_completion_tokens,
        "llm.usage.total-tokens": metrics.llm_total_tokens,
        "execution.time": metrics.execution_time_ms,
        "verify.status": metrics.verify_status,
    }

