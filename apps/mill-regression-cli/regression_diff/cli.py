"""CLI entry point for regression diff tool."""

import sys
import json
import traceback
from pathlib import Path
from typing import Optional, Dict, Tuple
import click

from regression_diff.archive import extract_archive, cleanup_extracted
from regression_diff.parser import parse_regression_artifact, ParsedResult
from regression_diff.builder_multi import build_multi_version_diff_table
from regression_diff.builder_action_grouped import build_action_grouped_report
from regression_diff.config import load_config
from regression_diff.formatters.markdown_multi import MarkdownMultiVersionFormatter
from regression_diff.formatters.csv_multi import CSVMultiVersionFormatter
from regression_diff.formatters.json_multi import JSONMultiVersionFormatter
from regression_diff.formatters.base import Formatter
from regression_diff.reports.config import ReportConfig
from regression_diff.reports.generator import MetricsReportGenerator
from regression_diff.reports.formatters.csv import CSVMetricsFormatter


def get_formatter(format_name: str) -> Formatter:
    """Get formatter by name."""
    formatters = {
        "markdown": MarkdownMultiVersionFormatter(),
        "csv": CSVMultiVersionFormatter(),
        "json": JSONMultiVersionFormatter(),
    }

    if format_name not in formatters:
        raise ValueError(
            f"Unknown format: {format_name}. Supported: {', '.join(formatters.keys())}"
        )

    return formatters[format_name]


@click.group()
def cli():
    """Regression diff tool for comparing test run artifacts."""
    pass


@cli.command(name="diff")
@click.argument("archive", type=click.Path(exists=True, path_type=Path))
@click.option(
    "--config",
    type=click.Path(exists=True, path_type=Path),
    help="Path to YAML configuration file",
)
@click.option(
    "--output",
    type=click.Path(path_type=Path),
    help="Output file or directory path (default: stdout)",
)
@click.option(
    "--format",
    "output_format",
    default="markdown",
    type=click.Choice(["markdown", "csv", "json"], case_sensitive=False),
    help="Output format (default: markdown)",
)
@click.option(
    "--group",
    help="Filter to specific group only (default: all groups)",
)
@click.option(
    "--versions",
    help="Specify version pair as 'baseline:candidate' (default: auto-detect adjacent)",
)
def diff_command(
    archive: Path,
    config: Optional[Path],
    output: Optional[Path],
    output_format: str,
    group: Optional[str],
    versions: Optional[str],
):
    """
    Compare regression test run artifacts and generate diff tables.

    ARCHIVE: Path to tar.gz file containing regression test results
    """
    try:
        # Load configuration
        app_config = load_config(config)

        # Extract archive
        structure = extract_archive(archive, app_config.get_version_selector())

        # Filter groups if specified
        groups = [group] if group else structure.get_groups()

        if not groups:
            click.echo("No groups found in archive", err=True)
            sys.exit(1)

        # Prepare output
        if output:
            output_path = Path(output)
            if output_path.is_dir() or len(groups) > 1:
                # Directory output or multiple groups
                output_path.mkdir(parents=True, exist_ok=True)
                use_directory = True
            else:
                # Single file output
                use_directory = False
        else:
            use_directory = False
            output_path = None

        # Process each group
        for group_name in groups:
            versions_list = structure.get_versions(group_name)

            if len(versions_list) < 2:
                click.echo(
                    f"Skipping group '{group_name}': need at least 2 versions, found {len(versions_list)}",
                    err=True,
                )
                continue

            # Determine versions to include
            if versions:
                # Use specified version pair (for backwards compatibility, but still build multi-version table)
                try:
                    baseline_ver, candidate_ver = versions.split(":", 1)
                    if baseline_ver not in versions_list or candidate_ver not in versions_list:
                        click.echo(
                            f"Version pair not found in group '{group_name}'",
                            err=True,
                        )
                        continue
                    selected_versions = [baseline_ver, candidate_ver]
                except ValueError:
                    click.echo("Invalid --versions format. Use 'baseline:candidate'", err=True)
                    sys.exit(1)
            else:
                # Use all versions from version selector
                selected_versions = versions_list

            # Collect data for all versions
            version_results: Dict[str, Dict[Tuple[str, str, str, str], ParsedResult]] = {}
            for version in selected_versions:
                version_files = structure.get_files(group_name, version)
                parsed_results = parse_regression_artifact(
                    group_name, version, version_files
                )
                version_results[version] = parsed_results

            # Build output based on format
            formatter = get_formatter(output_format)
            if output_format == "json":
                # Use action-grouped structure for JSON
                report = build_action_grouped_report(
                    group=group_name,
                    versions=selected_versions,
                    version_results=version_results,
                )
                formatted_output = formatter.format(report)
            else:
                # Use multi-version diff table for other formats
                diff_table = build_multi_version_diff_table(
                    group=group_name,
                    versions=selected_versions,
                    version_results=version_results,
                    config=app_config,
                )
                formatted_output = formatter.format(diff_table)

            # Write output
            if use_directory:
                # Write to file in directory
                versions_str = "_".join(selected_versions)
                output_file = (
                    output_path
                    / f"{group_name}_{versions_str}.{output_format}"
                )
                output_file.write_text(formatted_output, encoding="utf-8")
                click.echo(f"Written: {output_file}")
            elif output_path:
                # Write to single file
                output_path.write_text(formatted_output, encoding="utf-8")
                click.echo(f"Written: {output_path}")
            else:
                # Write to stdout
                click.echo(formatted_output)

        # Cleanup
        cleanup_extracted(structure)

    except Exception as e:
        click.echo(f"Error: {e}", err=True)
        traceback.print_exc()
        sys.exit(1)


@cli.command()
@click.argument("json_file", type=click.Path(exists=True, path_type=Path))
@click.option(
    "--metrics",
    required=True,
    help="Comma-separated list of hierarchical metric paths (e.g., 'action.success,action.outcome.metrics.execution.time')",
)
@click.option(
    "--versions",
    help="Optional comma-separated list of versions to include (if omitted, includes all versions)",
)
@click.option(
    "--format",
    "output_format",
    default="csv",
    type=click.Choice(["csv"], case_sensitive=False),
    help="Output format (default: csv)",
)
@click.option(
    "--output",
    type=click.Path(path_type=Path),
    help="Output file path (default: stdout)",
)
def report(
    json_file: Path,
    metrics: str,
    versions: Optional[str],
    output_format: str,
    output: Optional[Path],
):
    """
    Generate metrics report from action-grouped JSON file.
    
    JSON_FILE: Path to JSON file containing action-grouped data
    """
    try:
        # Parse metrics list
        metrics_list = [m.strip() for m in metrics.split(",") if m.strip()]
        if not metrics_list:
            click.echo("Error: At least one metric must be specified", err=True)
            sys.exit(1)
        
        # Parse versions list (if provided)
        versions_list = None
        if versions:
            versions_list = [v.strip() for v in versions.split(",") if v.strip()]
        
        # Load JSON data
        with open(json_file, "r", encoding="utf-8") as f:
            action_grouped_data = json.load(f)
        
        if not isinstance(action_grouped_data, list):
            click.echo("Error: JSON file must contain an array of action entries", err=True)
            sys.exit(1)
        
        # Create report configuration
        config = ReportConfig(
            metrics=metrics_list,
            versions=versions_list,
            output_format=output_format,
        )
        
        # Generate report
        generator = MetricsReportGenerator()
        report = generator.generate_from_action_grouped(action_grouped_data, config)
        
        # Format output
        if output_format == "csv":
            formatter = CSVMetricsFormatter()
            formatted_output = formatter.format(report)
        else:
            click.echo(f"Error: Unsupported format: {output_format}", err=True)
            sys.exit(1)
        
        # Write output
        if output:
            output.write_text(formatted_output, encoding="utf-8")
            click.echo(f"Written: {output}")
        else:
            click.echo(formatted_output)
    
    except Exception as e:
        click.echo(f"Error: {e}", err=True)
        traceback.print_exc()
        sys.exit(1)


# For backwards compatibility, keep main() as alias
# This allows the entry point to work with both old and new CLI structure
def main():
    """Main entry point for backwards compatibility."""
    # If called directly, use the group CLI
    cli()


if __name__ == "__main__":
    cli()

