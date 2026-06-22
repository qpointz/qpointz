#!/usr/bin/env python3
"""
Minimal LangChain ReAct agent over Mill MCP (Streamable HTTP).

Prerequisites:
  - mill-service running with ``skymill-ai`` and ``mill.ai.mcp.enabled=true``
  - ``OPENAI_API_KEY`` set
  - Optional auth env vars when Mill security is enabled (see README)

Usage:
  python skymill_agent.py "What tables exist in the skymill schema?"
"""

from __future__ import annotations

import asyncio
import os
import sys

from dotenv import load_dotenv
from langchain_core.tools import BaseTool
from langchain.agents import create_agent
from langchain_mcp_adapters.client import MultiServerMCPClient

from mill_mcp_config import mill_mcp_server_config


def _openai_safe_tools(tools: list[BaseTool]) -> list[BaseTool]:
    """OpenAI function names must match ``^[a-zA-Z0-9_-]+$``; Mill uses ``capability.tool``."""
    safe: list[BaseTool] = []
    for tool in tools:
        safe_name = tool.name.replace(".", "_")
        safe.append(tool if safe_name == tool.name else tool.model_copy(update={"name": safe_name}))
    return safe


def _require_openai_key() -> None:
    if not os.environ.get("OPENAI_API_KEY", "").strip():
        raise SystemExit("OPENAI_API_KEY is required. Copy .env.example to .env or export the variable.")


def _print_agent_result(result: object) -> None:
    if isinstance(result, dict):
        messages = result.get("messages")
        if messages:
            last = messages[-1]
            content = getattr(last, "content", last)
            print(content)
            return
    print(result)


async def run_agent(prompt: str) -> None:
    """Connect to Mill MCP over HTTP and run one user turn."""
    model = os.environ.get("OPENAI_MODEL", "gpt-4o-mini")
    client = MultiServerMCPClient({"mill": mill_mcp_server_config()})
    tools = _openai_safe_tools(await client.get_tools())
    if not tools:
        raise SystemExit("No MCP tools returned — check mill.ai.mcp.enabled and mill.ai.mcp.profile")

    agent = create_agent(f"openai:{model}", tools)
    result = await agent.ainvoke({"messages": [{"role": "user", "content": prompt}]})
    _print_agent_result(result)


def main() -> None:
    load_dotenv()
    _require_openai_key()
    prompt = " ".join(sys.argv[1:]).strip() or "List tables in the skymill schema."
    asyncio.run(run_agent(prompt))


if __name__ == "__main__":
    main()
