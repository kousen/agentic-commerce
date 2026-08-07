"""LAB 2 — the tests are the contract. Run `pytest -v`: one passes already (the tool
surface), five fail until you write the guard. The red sentences are your worklist."""

import re
import sys
from decimal import Decimal
from pathlib import Path

import anyio

from guard import Mandate, OrderPricing, authorize
from shop import COURSE_MANDATE, handle_buy, orders_placed

ACTIVE = Mandate("m-1", "ACTIVE", Decimal("50.00"))
REVOKED = Mandate("m-2", "REVOKED", Decimal("50.00"))


def test_an_in_mandate_purchase_is_authorized():
    """Autonomy is the success path."""
    assert authorize(ACTIVE, OrderPricing(Decimal("28.00"), Decimal("2.80"))) is None


def test_the_ceiling_is_denominated_in_what_the_customer_pays():
    """Subtotal 48.00 is under the 50.00 ceiling; the customer pays 52.80."""
    refusal = authorize(ACTIVE, OrderPricing(Decimal("48.00"), Decimal("4.80")))
    assert refusal is not None


def test_a_refusal_names_the_amount_and_the_cap():
    """The agent will retry regardless — tell it the constraint."""
    refusal = authorize(ACTIVE, OrderPricing(Decimal("65.00"), Decimal("6.50")))
    assert re.search(r"71\.5", refusal), "the refusal should name what the customer would have paid"
    assert re.search(r"50", refusal), "the refusal should name the ceiling"


def test_a_revoked_mandate_grants_nothing():
    assert authorize(REVOKED, OrderPricing(Decimal("5.00"), Decimal("0.50"))) is not None


def test_the_ceiling_holds_under_retry():
    """Same key, same order, one spend — duplicate delivery is normal control flow."""
    first = handle_buy(COURSE_MANDATE, 201, "retry-key-1")
    again = handle_buy(COURSE_MANDATE, 201, "retry-key-1")  # lost response, agent retries
    assert first["outcome"] == "COMPLETED"
    assert again == first
    assert orders_placed() == 1


def test_there_is_no_approval_tool_to_call():
    """Green before you start — this one is the scaffold's promise, over real MCP."""
    from mcp import ClientSession, StdioServerParameters
    from mcp.client.stdio import stdio_client

    server_path = str(Path(__file__).parent / "server.py")

    async def check():
        params = StdioServerParameters(command=sys.executable, args=[server_path])
        async with stdio_client(params) as (read, write):
            async with ClientSession(read, write) as session:
                await session.initialize()
                tools = await session.list_tools()
                names = sorted(t.name for t in tools.tools)
                assert names == ["buyTickets", "searchListings"]
                assert not any(re.search(r"approve|override|raise", n, re.I) for n in names)

    anyio.run(check)
