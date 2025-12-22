"""Report generator orchestrator."""

import warnings
from typing import List, Dict, Any
from regression_diff.reports.config import ReportConfig
from regression_diff.reports.models import MetricsReport, MetricRow
from regression_diff.reports.extractor import MetricExtractor


class MetricsReportGenerator:
    """Generates metrics reports from action-grouped JSON data."""

    def __init__(self):
        """Initialize the generator."""
        self.extractor = MetricExtractor()

    def generate_from_action_grouped(
        self, action_grouped_data: List[Dict[str, Any]], config: ReportConfig
    ) -> MetricsReport:
        """
        Generate a metrics report from action-grouped JSON data.
        
        Args:
            action_grouped_data: List of action entries (from JSON)
            config: Report configuration
        
        Returns:
            MetricsReport instance
        """
        report = MetricsReport()
        
        # Determine which versions to include and in what order
        all_versions = self._collect_all_versions(action_grouped_data)
        if config.versions:
            # Filter to only include specified versions in specified order
            selected_versions = [
                v for v in config.versions if v in all_versions
            ]
            # Warn about missing versions
            missing_versions = [v for v in config.versions if v not in all_versions]
            if missing_versions:
                warnings.warn(
                    f"Versions not found in data: {', '.join(missing_versions)}",
                    UserWarning
                )
        else:
            # Include all versions in their natural order
            selected_versions = sorted(all_versions)
        
        report.versions = selected_versions
        
        # Process each action entry
        for entry in action_grouped_data:
            action_info = entry.get("action", {})
            versions_data = entry.get("versions", [])
            
            group = action_info.get("group", "")
            scenario = action_info.get("scenario", "")
            action_key = action_info.get("key", "")
            
            # Extract each metric for this action
            for metric_path in config.metrics:
                version_values: Dict[str, Any] = {}
                
                # Extract metric value for each selected version
                for version in selected_versions:
                    # Find version data
                    version_data = next(
                        (v for v in versions_data if v.get("version") == version),
                        None
                    )
                    
                    if version_data:
                        action_data = version_data.get("action", {})
                        value = self.extractor.extract_metric(action_data, metric_path)
                        version_values[version] = value
                    else:
                        version_values[version] = None
                
                # Create row for this metric
                row = MetricRow(
                    group=group,
                    scenario=scenario,
                    action=action_key,
                    metric=metric_path,
                    version_values=version_values,
                )
                report.add_row(row)
        
        return report

    def _collect_all_versions(self, action_grouped_data: List[Dict[str, Any]]) -> set:
        """Collect all unique version names from the data."""
        versions = set()
        for entry in action_grouped_data:
            versions_data = entry.get("versions", [])
            for version_entry in versions_data:
                version = version_entry.get("version")
                if version:
                    versions.add(version)
        return versions
