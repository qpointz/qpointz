"""Metric extraction from action data."""

from typing import Any, Dict


class MetricExtractor:
    """Extracts metrics from action data using hierarchical paths."""

    @staticmethod
    def extract_metric(action_data: Dict[str, Any], metric_path: str) -> Any:
        """
        Extract a metric from action data using hierarchical dot-notation path.
        
        Args:
            action_data: The action data dictionary (from version entry's "action" field)
            metric_path: Hierarchical path like "action.success" or "action.outcome.metrics.execution.time"
        
        Returns:
            Extracted value or None if path doesn't exist
        
        Examples:
            - "action.success" -> action_data["success"]
            - "action.outcome.status" -> action_data["outcome"]["status"]
            - "action.outcome.metrics.execution.time" -> action_data["outcome"]["metrics"]["execution.time"]
        
        Note: The "action." prefix is removed since action_data is already the action object.
        Handles keys that contain dots (like "execution.time") by trying exact key match first.
        """
        if not metric_path:
            return None
        
        # Remove "action." prefix if present (action_data is already the action object)
        if metric_path.startswith("action."):
            remaining_path = metric_path[7:]  # Remove "action." prefix (7 chars)
        else:
            remaining_path = metric_path
        
        # Navigate through nested dictionaries
        # Handle keys that may contain dots (like "execution.time")
        current = action_data
        path_parts = remaining_path.split(".")
        
        i = 0
        while i < len(path_parts):
            if not isinstance(current, dict):
                return None
            
            # Try to find the key, handling cases where keys contain dots
            # First, try the current part as-is
            part = path_parts[i]
            if part in current:
                current = current[part]
                i += 1
                continue
            
            # If not found, try combining with next parts (for keys like "execution.time")
            # Try progressively longer combinations
            combined = part
            found = False
            for j in range(i + 1, len(path_parts)):
                combined += "." + path_parts[j]
                if combined in current:
                    current = current[combined]
                    i = j + 1
                    found = True
                    break
            
            if not found:
                return None
        
        return current
