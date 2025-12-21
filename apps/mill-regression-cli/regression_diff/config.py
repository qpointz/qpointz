"""Configuration loading and defaults."""

import yaml
from pathlib import Path
from typing import Dict, Any, Optional
from regression_diff.metric_comparers.base import MetricConfig
from regression_diff.version_selector import VersionSelector, AllVersionsSelector


class Config:
    """Configuration for regression diff tool."""

    def __init__(self, config_dict: Optional[Dict[str, Any]] = None):
        """Initialize configuration with defaults."""
        if config_dict is None:
            config_dict = {}

        self._metric_configs: Dict[str, MetricConfig] = {}
        self._version_selector: VersionSelector = AllVersionsSelector()

        # Load metric configurations
        metrics_config = config_dict.get("metrics", {})
        for metric_name, metric_dict in metrics_config.items():
            self._metric_configs[metric_name] = MetricConfig(
                metric_name=metric_name,
                threshold=metric_dict.get("threshold"),
                threshold_type=metric_dict.get("threshold_type", "absolute"),
                direction=metric_dict.get("direction", "any"),
                warn_on_threshold_exceeded=metric_dict.get(
                    "warn_on_threshold_exceeded", True
                ),
                tolerate_small_improvements=metric_dict.get(
                    "tolerate_small_improvements", False
                ),
            )

        # Set defaults for common metrics if not specified
        self._set_default_metrics()

    def _set_default_metrics(self) -> None:
        """Set default metric configurations."""
        defaults = {
            "llm.usage.prompt-tokens": MetricConfig(
                metric_name="llm.usage.prompt-tokens",
                direction="less_is_better",
                threshold=5000.0,
                threshold_type="absolute",
                warn_on_threshold_exceeded=True,
            ),
            "llm.usage.total-tokens": MetricConfig(
                metric_name="llm.usage.total-tokens",
                direction="less_is_better",
                threshold=10000.0,
                threshold_type="absolute",
                warn_on_threshold_exceeded=True,
            ),
            "execution.time": MetricConfig(
                metric_name="execution.time",
                direction="less_is_better",
                threshold=15000.0,
                threshold_type="absolute",
                warn_on_threshold_exceeded=True,
            ),
            "data.size": MetricConfig(
                metric_name="data.size",
                direction="equal",
                threshold=0.0,
                threshold_type="absolute",
                warn_on_threshold_exceeded=True,
            ),
            "intent": MetricConfig(
                metric_name="intent",
                direction="equal",
                warn_on_threshold_exceeded=False,  # Intent change = FAIL
            ),
            "sqlShape.hasJoin": MetricConfig(
                metric_name="sqlShape.hasJoin",
                direction="equal",
                warn_on_threshold_exceeded=False,  # Unexpected join = FAIL
            ),
        }

        # Only set if not already configured
        for metric_name, metric_config in defaults.items():
            if metric_name not in self._metric_configs:
                self._metric_configs[metric_name] = metric_config

    def get_metric_config(self, metric_name: str) -> MetricConfig:
        """Get metric configuration, with defaults."""
        if metric_name in self._metric_configs:
            return self._metric_configs[metric_name]

        # Return default config
        return MetricConfig(metric_name=metric_name)

    def set_version_selector(self, selector: VersionSelector) -> None:
        """Set version selector."""
        self._version_selector = selector

    def get_version_selector(self) -> VersionSelector:
        """Get version selector."""
        return self._version_selector


def load_config(config_path: Optional[Path] = None) -> Config:
    """
    Load configuration from YAML file.

    Args:
        config_path: Path to config file (uses defaults if None)

    Returns:
        Config instance
    """
    if config_path is None or not config_path.exists():
        return Config()

    try:
        with open(config_path, "r", encoding="utf-8") as f:
            config_dict = yaml.safe_load(f) or {}
        return Config(config_dict)
    except Exception as e:
        raise ValueError(f"Failed to load config from {config_path}: {e}") from e

