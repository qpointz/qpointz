"""CLI entry point for regression diff tool."""

import sys
from pathlib import Path
from typing import Optional
import click

from regression_diff.archive import extract_archive, cleanup_extracted
from regression_diff.parser import parse_regression_artifact
from regression_diff.matcher import match_results
from regression_diff.comparator import compare_results
from regression_diff.builder import build_diff_table
from regression_diff.builder_multi import build_multi_version_diff_table
from regression_diff.config import load_config, Config
from regression_diff.version_selector import AllVersionsSelector
from regression_diff.formatters.markdown import MarkdownFormatter
from regression_diff.formatters.markdown_multi import MarkdownMultiVersionFormatter
from regression_diff.formatters.csv import CSVFormatter
from regression_diff.formatters.csv_multi import CSVMultiVersionFormatter
from regression_diff.formatters.json import JSONFormatter
from regression_diff.formatters.base import Formatter
from typing import Dict, Tuple


def get_formatter(format_name: str, multi_version: bool = False) -> Formatter:
    """Get formatter by name."""
    if multi_version:
        formatters = {
            "markdown": MarkdownMultiVersionFormatter(),
            "csv": CSVMultiVersionFormatter(),
            # TODO: Add JSON multi-version formatter if needed
            "json": JSONFormatter(),  # Fallback to single-version for now
        }
    else:
        formatters = {
            "markdown": MarkdownFormatter(),
            "csv": CSVFormatter(),
            "json": JSONFormatter(),
        }

    if format_name not in formatters:
        raise ValueError(
            f"Unknown format: {format_name}. Supported: {', '.join(formatters.keys())}"
        )

    return formatters[format_name]


@click.command()
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
@click.option(
    "--version-selector",
    default="all",
    help="Version selector strategy (default: all)",
)
def main(
    archive: Path,
    config: Optional[Path],
    output: Optional[Path],
    output_format: str,
    group: Optional[str],
    versions: Optional[str],
    version_selector: str,
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
            from regression_diff.parser import ParsedResult
            version_results: Dict[str, Dict[Tuple[str, str, str, str], ParsedResult]] = {}
            for version in selected_versions:
                version_files = structure.get_files(group_name, version)
                parsed_results = parse_regression_artifact(
                    group_name, version, version_files
                )
                version_results[version] = parsed_results

            # Build multi-version diff table
            formatter = get_formatter(output_format, multi_version=True)
            diff_table = build_multi_version_diff_table(
                group=group_name,
                versions=selected_versions,
                version_results=version_results,
                config=app_config,
            )

            # Format output
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
        import traceback
        traceback.print_exc()
        sys.exit(1)


if __name__ == "__main__":
    main()

