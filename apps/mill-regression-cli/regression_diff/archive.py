"""Archive extraction and structure discovery."""

import tarfile
import tempfile
from pathlib import Path
from typing import Dict, List, Optional
from collections import defaultdict

from regression_diff.version_selector import VersionSelector, AllVersionsSelector


class ArchiveStructure:
    """Represents the discovered structure of a regression archive."""

    def __init__(self, extracted_path: Path):
        """Initialize with path to extracted archive."""
        self.extracted_path = extracted_path
        self._structure: Dict[str, Dict[str, List[Path]]] = defaultdict(
            lambda: defaultdict(list)
        )

    def add_file(self, group: str, version: str, file_path: Path) -> None:
        """Add a JSON file to the structure."""
        self._structure[group][version].append(file_path)

    def get_structure(self) -> Dict[str, Dict[str, List[Path]]]:
        """Get the discovered structure."""
        return dict(self._structure)

    def get_groups(self) -> List[str]:
        """Get list of groups."""
        return sorted(self._structure.keys())

    def get_versions(self, group: str) -> List[str]:
        """Get sorted list of versions for a group."""
        if group not in self._structure:
            return []
        return sorted(self._structure[group].keys())

    def get_files(self, group: str, version: str) -> List[Path]:
        """Get list of files for a group/version."""
        if group not in self._structure or version not in self._structure[group]:
            return []
        return self._structure[group][version]


def extract_archive(
    archive_path: Path, version_selector: Optional[VersionSelector] = None
) -> ArchiveStructure:
    """
    Extract tar.gz archive and discover structure.

    Args:
        archive_path: Path to tar.gz file
        version_selector: Optional version selector to filter versions

    Returns:
        ArchiveStructure with discovered groups and versions

    Raises:
        ValueError: If archive cannot be extracted or structure is invalid
    """
    if not archive_path.exists():
        raise ValueError(f"Archive not found: {archive_path}")

    if not archive_path.suffix == ".gz" or not archive_path.name.endswith(".tar.gz"):
        raise ValueError(f"Invalid archive format (expected .tar.gz): {archive_path}")

    if version_selector is None:
        version_selector = AllVersionsSelector()

    # Create temporary directory for extraction
    temp_dir = tempfile.mkdtemp(prefix="regression_diff_")
    extracted_path = Path(temp_dir)

    try:
        # Extract archive
        with tarfile.open(archive_path, "r:gz") as tar:
            tar.extractall(extracted_path)

        structure = ArchiveStructure(extracted_path)

        # Discover structure: /<group>/<version>/<*.json>
        # Look for pattern: reports/<group>/<version>/*.json
        reports_dir = extracted_path / "reports"
        if not reports_dir.exists():
            # Try root level
            reports_dir = extracted_path

        for json_file in reports_dir.rglob("*.json"):
            # Extract group and version from path
            # Expected: reports/<group>/<version>/<file>.json
            relative_path = json_file.relative_to(reports_dir)
            parts = relative_path.parts

            if len(parts) < 2:
                # Skip files not in group/version structure
                continue

            group = parts[0]
            version = parts[1]

            structure.add_file(group, version, json_file)

        # Apply version selector to filter versions
        filtered_structure = ArchiveStructure(extracted_path)
        for group in structure.get_groups():
            versions = structure.get_versions(group)
            selected_versions = version_selector.select_versions(
                versions, {"group": group}
            )

            for version in selected_versions:
                files = structure.get_files(group, version)
                for file_path in files:
                    filtered_structure.add_file(group, version, file_path)

        return filtered_structure

    except tarfile.TarError as e:
        raise ValueError(f"Failed to extract archive: {e}") from e
    except Exception as e:
        raise ValueError(f"Failed to process archive: {e}") from e


def cleanup_extracted(structure: ArchiveStructure) -> None:
    """Clean up extracted archive temporary directory."""
    import shutil

    if structure.extracted_path.exists():
        shutil.rmtree(structure.extracted_path, ignore_errors=True)

