import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/// Checklist line: idempotency on every state-changing tool.
///
/// Why this stopped being optional in 2026: MRTR approval re-issues THE SAME tool call
/// after the human answers, and agents retry in milliseconds when a response is lost.
/// Duplicate delivery is normal control flow now, not an error condition — and a
/// duplicate order is a liability, not a bug report.
///
/// Run it:  java IdempotencyKey.java
public class IdempotencyKey {

    record Order(String orderNumber, long listingId, BigDecimal totalCharged) { }

    static final class CheckoutService {
        // ponytail: in-memory map — production is a DB unique constraint on the key
        // column, so two racing requests can't both miss the cache and both insert.
        private final Map<String, Order> byIdempotencyKey = new ConcurrentHashMap<>();
        private int ordersPlaced = 0;
        private BigDecimal spendRecorded = BigDecimal.ZERO;

        /// The whole pattern: same key → the ORIGINAL order comes back, and the
        /// side effects (order creation, spend accounting) happen exactly once.
        synchronized Order checkout(String idempotencyKey, long listingId, BigDecimal total) {
            Order existing = byIdempotencyKey.get(idempotencyKey);
            if (existing != null) return existing;     // a retry, not a second purchase

            Order order = new Order("MH-2026-%04d".formatted(++ordersPlaced), listingId, total);
            spendRecorded = spendRecorded.add(total);  // mandate accounting: once per order
            byIdempotencyKey.put(idempotencyKey, order);
            return order;
        }

        int ordersPlaced()        { return ordersPlaced; }
        BigDecimal spendRecorded() { return spendRecorded; }
    }

    // ---- self-check ---------------------------------------------------------------------

    public static void main(String[] args) {
        var service = new CheckoutService();
        var price = new BigDecimal("35.00");

        Order first  = service.checkout("key-abc", 98203, price);
        Order retry  = service.checkout("key-abc", 98203, price);   // lost response, agent retries

        check("a retry with the same key returns the same order number",
                first.orderNumber().equals(retry.orderNumber()));
        check("one order was placed, not two",
                service.ordersPlaced() == 1);
        check("the mandate was charged once — the ceiling holds under retry",
                service.spendRecorded().compareTo(price) == 0);

        Order second = service.checkout("key-xyz", 98203, price);   // genuinely new purchase
        check("a different key is a different purchase",
                !second.orderNumber().equals(first.orderNumber()) && service.ordersPlaced() == 2);

        System.out.println("OK — duplicate delivery was normal control flow, and nothing doubled");
    }

    static void check(String sentence, boolean holds) {
        if (!holds) throw new AssertionError("FAILED: " + sentence);
        System.out.println("  ✓ " + sentence);
    }
}
