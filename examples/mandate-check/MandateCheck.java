import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;

/// Front Door step 4 — the mandate check.
///
/// The single most important property of this file: it is deterministic code over
/// structured fields. No model is consulted. The model may have *proposed* the purchase;
/// it does not get a vote on whether the purchase is authorized.
///
/// Run it:  java MandateCheck.java
public class MandateCheck {

    /// The permission slip. Every field was chosen by the customer, is readable back to
    /// them, and can be revoked. Authority comes from this artifact — not from anything
    /// the agent says.
    record Mandate(
            String mandateId,
            String agentId,
            Status status,
            BigDecimal maxSpendPerTransaction,
            BigDecimal maxSpendTotal,
            BigDecimal totalSpent,          // accounting: recorded on confirm, reversed on cancel
            Set<String> allowedCategories,  // empty set = no restriction
            Instant expiresAt) {

        enum Status { ACTIVE, REVOKED, EXPIRED }

        BigDecimal remainingBudget() {
            return maxSpendTotal.subtract(totalSpent);
        }
    }

    /// What the purchase will actually cost the customer, assembled in ONE place.
    /// The units lesson, learned from two real orders: a $35 ceiling that validates the
    /// subtotal authorizes a $38.50 charge. Whatever number the customer said, the
    /// boundary evaluates THAT number — the all-in total.
    record OrderPricing(BigDecimal subtotal, BigDecimal serviceFee) {
        BigDecimal total() {
            return subtotal.add(serviceFee);
        }
    }

    /// The result carries a reason the agent can relay verbatim. The agent will retry
    /// regardless — tell it the constraint, or it will invent one for your customer.
    record Decision(boolean authorized, String reason) {
        static Decision yes() {
            return new Decision(true, "authorized");
        }
        static Decision no(String reason) {
            return new Decision(false, reason);
        }
    }

    /// The check itself. Called at cart time AND again at confirmation — time passes
    /// between those moments, and revocation or cumulative spend may have moved.
    static Decision authorize(Mandate mandate, OrderPricing pricing, String categorySlug, Instant now) {
        if (mandate.status() != Mandate.Status.ACTIVE) {
            return Decision.no("No active mandate: mandate %s is %s"
                    .formatted(mandate.mandateId(), mandate.status()));
        }
        if (now.isAfter(mandate.expiresAt())) {
            return Decision.no("Mandate %s expired at %s"
                    .formatted(mandate.mandateId(), mandate.expiresAt()));
        }
        if (!mandate.allowedCategories().isEmpty()
                && !mandate.allowedCategories().contains(categorySlug)) {
            return Decision.no("Mandate %s does not cover category '%s' (allowed: %s)"
                    .formatted(mandate.mandateId(), categorySlug, mandate.allowedCategories()));
        }

        BigDecimal charged = pricing.total();   // the all-in amount — never the subtotal
        if (charged.compareTo(mandate.maxSpendPerTransaction()) > 0) {
            return Decision.no("Amount %s exceeds the %s per-transaction cap on mandate %s"
                    .formatted(charged, mandate.maxSpendPerTransaction(), mandate.mandateId()));
        }
        if (charged.compareTo(mandate.remainingBudget()) > 0) {
            return Decision.no("Amount %s exceeds remaining budget %s on mandate %s"
                    .formatted(charged, mandate.remainingBudget(), mandate.mandateId()));
        }
        return Decision.yes();
    }

    // ---- self-check: each assertion is a sentence of the contract ----------------------

    public static void main(String[] args) {
        Instant now = Instant.parse("2026-08-07T12:00:00Z");
        Mandate mandate = new Mandate("m-1", "demo-agent", Mandate.Status.ACTIVE,
                new BigDecimal("35.00"), new BigDecimal("100.00"), BigDecimal.ZERO,
                Set.of(), now.plusSeconds(86_400));

        check("an in-mandate purchase is authorized — autonomy is the success path",
                authorize(mandate, new OrderPricing(new BigDecimal("28.00"), new BigDecimal("2.80")),
                        "concerts", now).authorized());

        // The units lesson: subtotal $32.15 is under the $35 ceiling; the charge is not.
        Decision unitsMismatch = authorize(mandate,
                new OrderPricing(new BigDecimal("32.15"), new BigDecimal("3.22")), "concerts", now);
        check("the ceiling is denominated in what the customer pays, not the subtotal",
                !unitsMismatch.authorized());
        check("the refusal names the amount and the cap, so the agent can relay it",
                unitsMismatch.reason().contains("35.37") && unitsMismatch.reason().contains("35.00"));

        check("a revoked mandate grants nothing",
                !authorize(withStatus(mandate, Mandate.Status.REVOKED),
                        new OrderPricing(new BigDecimal("5.00"), new BigDecimal("0.50")),
                        "concerts", now).authorized());

        check("yesterday's authority is not today's — expiry is checked at spend time",
                !authorize(mandate, new OrderPricing(new BigDecimal("5.00"), new BigDecimal("0.50")),
                        "concerts", now.plusSeconds(200_000)).authorized());

        Mandate concertsOnly = new Mandate("m-2", "demo-agent", Mandate.Status.ACTIVE,
                new BigDecimal("35.00"), new BigDecimal("100.00"), BigDecimal.ZERO,
                Set.of("concerts"), now.plusSeconds(86_400));
        check("a category restriction holds",
                !authorize(concertsOnly, new OrderPricing(new BigDecimal("5.00"), new BigDecimal("0.50")),
                        "sports", now).authorized());

        Mandate nearlySpent = new Mandate("m-3", "demo-agent", Mandate.Status.ACTIVE,
                new BigDecimal("35.00"), new BigDecimal("100.00"), new BigDecimal("80.00"),
                Set.of(), now.plusSeconds(86_400));
        check("the cumulative budget holds even when the per-transaction cap would pass",
                !authorize(nearlySpent, new OrderPricing(new BigDecimal("30.00"), new BigDecimal("3.00")),
                        "concerts", now).authorized());

        System.out.println("OK — the mandate boundary held in every case");
    }

    static Mandate withStatus(Mandate m, Mandate.Status s) {
        return new Mandate(m.mandateId(), m.agentId(), s, m.maxSpendPerTransaction(),
                m.maxSpendTotal(), m.totalSpent(), m.allowedCategories(), m.expiresAt());
    }

    static void check(String sentence, boolean holds) {
        if (!holds) throw new AssertionError("FAILED: " + sentence);
        System.out.println("  ✓ " + sentence);
    }
}
