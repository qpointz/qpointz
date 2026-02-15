"""Proto stub generator for mill-py.

Generates Python protobuf messages, gRPC stubs, and type stubs (.pyi)
from the canonical proto definitions in ../../proto/ into mill/_proto/.

Usage:
    python codegen.py          # from clients/mill-py/
    make proto                 # if wired in Makefile (future)

Requirements:
    pip install grpcio-tools   # provides grpc_tools.protoc
"""
from __future__ import annotations

import glob
import os
import shutil
import subprocess
import sys
from pathlib import Path


def main() -> None:
    # Resolve directories relative to this script
    script_dir = Path(__file__).resolve().parent
    proto_root = (script_dir / "../../proto").resolve()
    out_dir = (script_dir / "mill/_proto").resolve()

    if not proto_root.is_dir():
        print(f"ERROR: proto source directory not found: {proto_root}", file=sys.stderr)
        sys.exit(1)

    # Clean and recreate output directory
    if out_dir.exists():
        shutil.rmtree(out_dir)
    out_dir.mkdir(parents=True)

    # Collect all .proto files (deduplicated)
    proto_files = sorted({
        str(p) for p in proto_root.rglob("*.proto")
    })

    if not proto_files:
        print(f"ERROR: no .proto files found under {proto_root}", file=sys.stderr)
        sys.exit(1)

    print(f"Proto root : {proto_root}")
    print(f"Output dir : {out_dir}")
    print(f"Proto files: {len(proto_files)}")

    # Find grpc_tools well-known protos include path
    try:
        import grpc_tools
        grpc_tools_dir = Path(grpc_tools.__file__).parent / "_proto"
        if grpc_tools_dir.is_dir():
            well_known_include = str(grpc_tools_dir)
        else:
            well_known_include = None
    except ImportError:
        print("ERROR: grpcio-tools not installed. Run: pip install grpcio-tools", file=sys.stderr)
        sys.exit(1)

    # Build protoc arguments
    args = [
        sys.executable, "-m", "grpc_tools.protoc",
        f"--proto_path={proto_root}",
        f"--python_out={out_dir}",
        f"--grpc_python_out={out_dir}",
        f"--pyi_out={out_dir}",
    ]

    if well_known_include:
        args.append(f"--proto_path={well_known_include}")

    args.extend(proto_files)

    print(f"\nRunning protoc...")
    result = subprocess.run(args, capture_output=True, text=True)

    if result.stdout:
        print(result.stdout)
    if result.stderr:
        print(result.stderr, file=sys.stderr)
    if result.returncode != 0:
        print(f"ERROR: protoc failed with exit code {result.returncode}", file=sys.stderr)
        sys.exit(result.returncode)

    # Generate __init__.py files in all output directories
    _create_init_files(out_dir)

    # Fix imports in generated files: protobuf codegen uses absolute imports
    # like "import common_pb2" but we need "from mill._proto import common_pb2"
    # or relative imports "from . import common_pb2"
    _fix_imports(out_dir, proto_root)

    print(f"\nGeneration complete. Stubs written to {out_dir}")


def _create_init_files(root: Path) -> None:
    """Create __init__.py in root and every subdirectory."""
    for dirpath, dirnames, _filenames in os.walk(root):
        init_file = Path(dirpath) / "__init__.py"
        if not init_file.exists():
            init_file.write_text("")
            print(f"  Created {init_file.relative_to(root.parent.parent)}")


def _fix_imports(out_dir: Path, proto_root: Path) -> None:
    """Fix generated import statements to use relative imports.

    The protobuf compiler generates imports like:
        import common_pb2 as common__pb2
        from substrait import plan_pb2 as substrait_dot_plan__pb2

    We need to convert these to relative imports within the mill._proto package:
        from . import common_pb2 as common__pb2
        from .substrait import plan_pb2 as substrait_dot_plan__pb2
    """
    # Collect all proto module basenames (without .proto) to know what to fix
    proto_modules = set()
    for proto_file in glob.glob(str(proto_root / "*.proto")):
        proto_modules.add(Path(proto_file).stem + "_pb2")
    # Also collect gRPC service modules
    proto_modules_grpc = {m.replace("_pb2", "_pb2_grpc") for m in proto_modules}

    # Substrait subpackage modules
    substrait_modules = set()
    for proto_file in glob.glob(str(proto_root / "substrait/*.proto")):
        substrait_modules.add(Path(proto_file).stem + "_pb2")
    for proto_file in glob.glob(str(proto_root / "substrait/**/*.proto"), recursive=True):
        substrait_modules.add(Path(proto_file).stem + "_pb2")

    for py_file in glob.glob(str(out_dir / "**/*.py"), recursive=True):
        py_path = Path(py_file)
        if py_path.name == "__init__.py":
            continue

        content = py_path.read_text()
        original = content

        # Determine the depth of this file relative to out_dir
        rel = py_path.relative_to(out_dir)
        depth = len(rel.parts) - 1  # 0 for root, 1 for substrait/, etc.

        # Fix top-level proto imports in files at the root of _proto/
        # e.g. "import common_pb2 as common__pb2" -> "from . import common_pb2 as common__pb2"
        for mod in proto_modules | proto_modules_grpc:
            # "import <mod> as ..." pattern
            content = content.replace(
                f"import {mod} as ",
                f"from {'.' * (depth + 1)} import {mod} as "
            )

        # Fix substrait imports
        # e.g. "from substrait import plan_pb2" -> "from .substrait import plan_pb2"
        # or in substrait/ files: "from substrait.extensions import ..."
        if depth == 0:
            content = content.replace(
                "from substrait import ",
                "from .substrait import "
            )
            content = content.replace(
                "from substrait.extensions import ",
                "from .substrait.extensions import "
            )
        elif depth >= 1:
            # Files inside substrait/ — fix relative to their position
            content = content.replace(
                "from substrait import ",
                "from . import "
            )
            content = content.replace(
                "from substrait.extensions import ",
                "from .extensions import "
            )

        if content != original:
            py_path.write_text(content)
            print(f"  Fixed imports: {rel}")


if __name__ == "__main__":
    main()
