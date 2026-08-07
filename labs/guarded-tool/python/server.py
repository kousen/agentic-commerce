"""Scaffolding — the MCP wiring. Two tools, over stdio, using the official SDK.

Count the tools. There is no approvePurchase, no raiseCeiling, no overrideMandate.
The boundary is not something the agent negotiates with; it's the shape of its world.

Run it yourself:            python server.py
Connect a real agent to it: claude --mcp-config mcp-config.json --strict-mcp-config
"""

import json
from dataclasses import asdict

from mcp.server import MCPServer

from shop import COURSE_MANDATE, LISTINGS, handle_buy

mcp = MCPServer("guarded-ticket-shop")


@mcp.tool()
def searchListings() -> str:
    """List the available ticket listings with all-in pricing (subtotal plus service fee)."""
    return json.dumps([{**asdict(l), "subtotal": str(l.subtotal), "service_fee": str(l.service_fee)}
                       for l in LISTINGS], indent=2)


@mcp.tool()
def buyTickets(listingId: int, idempotencyKey: str) -> str:
    """Buy one listing on the customer's behalf under their mandate. Refusals state the
    constraint. Pass a fresh idempotencyKey per purchase; reuse the SAME key when retrying."""
    return json.dumps(handle_buy(COURSE_MANDATE, listingId, idempotencyKey))


if __name__ == "__main__":
    mcp.run()
