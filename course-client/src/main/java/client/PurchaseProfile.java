package client;

import java.math.BigDecimal;

/// The customer's declared intent, as a versioned artifact (Module 2, §2.3).
/// The LLM may PROPOSE one of these; deterministic code validates it; the mandate
/// attaches to it. This client consumes an already-validated profile.
public record PurchaseProfile(
        String version,
        String eventQuery,
        BigDecimal maxTotalPrice,   // all-in, the customer's units — never the subtotal
        String preferredSection) {  // null = no preference

    public static PurchaseProfile of(String eventQuery, String maxTotalPrice, String preferredSection) {
        return new PurchaseProfile("v1", eventQuery, new BigDecimal(maxTotalPrice), preferredSection);
    }
}
