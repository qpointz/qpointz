#!/usr/bin/env python3
"""
Lightweight MCP smoke test (no LLM).

Verifies Streamable HTTP connectivity and that schema-exploration tools are exposed.
"""

from __future__ import annotations

import asyncio
import sys

from dotenv import load_dotenv
from langchain_mcp_adapters.client import MultiServerMCPClient

from mill_mcp_config import mill_mcp_server_config

EXPECTED_TOOLS = (
    "schema.list_schemas",
    "schema.list_tables",
    "schema.list_columns",
)


async def smoke() -> int:
    client = MultiServerMCPClient({"mill": mill_mcp_server_config()})
    tools = await client.get_tools()
    names = sorted({tool.name for tool in tools})
    print(f"Connected to Mill MCP — {len(names)} tools")
    for name in names:
        print(f"  - {name}")

    missing = [name for name in EXPECTED_TOOLS if name not in names]
    if missing:
        print(f"Missing expected tools: {', '.join(missing)}", file=sys.stderr)
        return 1
    print("OK: schema exploration tools present")
    return 0


def main() -> None:
    load_dotenv()
    raise SystemExit(asyncio.run(smoke()))


if __name__ == "__main__":
    main()
