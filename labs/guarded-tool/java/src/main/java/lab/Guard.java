package lab;

import java.math.BigDecimal;

/// LAB 2 — the guard. This file is YOURS.
///
/// Everything else in this lab is scaffolding: the MCP server, the listings, the
/// idempotency store. The guard is the part the whole course has been circling —
/// the deterministic authorization decision that runs before money moves.
public final class Guard {

    public enum Status { ACTIVE, REVOKED }

    /// The ceiling is in the customer's units: what they PAY, all-in.
    public record Mandate(String mandateId, Status status, BigDecimal maxSpendPerTransaction) { }

    public record OrderPricing(BigDecimal subtotal, BigDecimal serviceFee) { }

    /// YOUR TURN (5–10 lines).
    ///
    /// Return null to authorize the purchase, or a refusal string the agent can relay
    /// to the customer. The tests state the contract:
    ///
    ///   1. A revoked mandate grants nothing — check status first.
    ///   2. The ceiling is denominated in what the customer pays: subtotal PLUS fee.
    ///      (One of the listings is priced to catch you if you check the subtotal.)
    ///   3. A refusal names both numbers — the amount and the cap — because the agent
    ///      will retry regardless; tell it the constraint or it will invent one.
    public static String authorize(Mandate mandate, OrderPricing pricing) {
        throw new UnsupportedOperationException("YOUR TURN — write the guard (labs.md, Lab 2)");
    }

    private Guard() { }
}
