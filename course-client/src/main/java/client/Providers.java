package client;

import java.math.BigDecimal;
import java.util.List;

/// The provider abstraction the sourcing contract quantifies over:
/// "search EVERY eligible provider" only means something once providers are values.
public final class Providers {

    /// A listing normalized to the shape the policy ranks. Note what is absent:
    /// no free-text description. Seller prose never enters a sourcing decision.
    public record ProviderListing(
            String provider,
            long listingId,
            String event,
            String section,
            String row,
            String seat,
            BigDecimal subtotal,
            BigDecimal serviceFee) {

        public BigDecimal total() {
            return subtotal.add(serviceFee);
        }

        /// Two providers offering the same physical seat are one listing, not two.
        public String seatKey() {
            return event + "|" + section + "|" + row + "|" + seat;
        }
    }

    public interface TicketProvider {
        String name();
        List<ProviderListing> search(PurchaseProfile profile) throws Exception;
    }

    private Providers() { }
}
