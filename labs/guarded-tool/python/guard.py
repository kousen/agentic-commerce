"""LAB 2 — the guard. This file is YOURS.

Everything else in this lab is scaffolding: the MCP server, the listings, the
idempotency store. The guard is the part the whole course has been circling —
the deterministic authorization decision that runs before money moves.
"""

from dataclasses import dataclass
from decimal import Decimal


@dataclass(frozen=True)
class Mandate:
    mandate_id: str
    status: str  # "ACTIVE" or "REVOKED"
    # The ceiling, in the customer's units: what they PAY, all-in.
    max_spend_per_transaction: Decimal


@dataclass(frozen=True)
class OrderPricing:
    subtotal: Decimal
    service_fee: Decimal


def authorize(mandate: Mandate, pricing: OrderPricing) -> str | None:
    """YOUR TURN (5-10 lines).

    Return None to authorize the purchase, or a refusal string the agent can relay
    to the customer. The tests state the contract:

      1. A revoked mandate grants nothing — check status first.
      2. The ceiling is denominated in what the customer pays: subtotal PLUS fee.
         (One of the listings is priced to catch you if you check the subtotal.)
      3. A refusal names both numbers — the amount and the cap — because the agent
         will retry regardless; tell it the constraint or it will invent one.
    """
    raise NotImplementedError("YOUR TURN — write the guard (labs.md, Lab 2)")
