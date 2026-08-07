// LAB 2 — the guard. This file is YOURS.
//
// Everything else in this lab is scaffolding: the MCP server, the listings, the
// idempotency store. The guard is the part the whole course has been circling —
// the deterministic authorization decision that runs before money moves.

export interface Mandate {
  mandateId: string;
  status: "ACTIVE" | "REVOKED";
  /** The ceiling, in the customer's units: what they PAY, all-in. */
  maxSpendPerTransaction: number;
}

export interface OrderPricing {
  subtotal: number;
  serviceFee: number;
}

/**
 * YOUR TURN (5–10 lines).
 *
 * Return `null` to authorize the purchase, or a refusal string the agent can relay
 * to the customer. The tests state the contract:
 *
 *   1. A revoked mandate grants nothing — check status first.
 *   2. The ceiling is denominated in what the customer pays: subtotal PLUS fee.
 *      (One of the listings is priced to catch you if you check the subtotal.)
 *   3. A refusal names both numbers — the amount and the cap — because the agent
 *      will retry regardless; tell it the constraint or it will invent one.
 */
export function authorize(mandate: Mandate, pricing: OrderPricing): string | null {
  throw new Error("YOUR TURN — write the guard (labs.md, Lab 2)");
}
