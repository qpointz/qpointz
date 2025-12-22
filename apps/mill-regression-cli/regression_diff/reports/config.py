"""Report configuration."""

from dataclasses import dataclass
from typing import List, Optional


@dataclass
class ReportConfig:
    """Configuration for report generation."""

    metrics: List[str]  # List of hierarchical metric paths
    versions: Optional[List[str]] = None  # Optional list of versions to include (None = all)
    output_format: str = "csv"  # Output format (csv, etc.)
