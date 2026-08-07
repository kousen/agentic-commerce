package lab;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import lab.Guard.Mandate;
import lab.Guard.OrderPricing;
import lab.Guard.Status;
import lab.Shop.BuyResult;

import static org.assertj.core.api.Assertions.assertThat;

/// LAB 2 — the tests are the contract. Run ./gradlew test: one passes already (the
/// tool surface), five fail until you write the guard. The red sentences are your worklist.
class GuardedToolLabTest {

    static final Mandate ACTIVE =
            new Mandate("m-1", Status.ACTIVE, new BigDecimal("50.00"));
    static final Mandate REVOKED =
            new Mandate("m-2", Status.REVOKED, new BigDecimal("50.00"));

    @Test
    @DisplayName("an in-mandate purchase is authorized — autonomy is the success path")
    void anInMandatePurchaseIsAuthorized() {
        assertThat(Guard.authorize(ACTIVE,
                new OrderPricing(new BigDecimal("28.00"), new BigDecimal("2.80"))))
                .isNull();
    }

    @Test
    @DisplayName("the ceiling is denominated in what the customer pays, not the subtotal")
    void theCeilingIsDenominatedInWhatTheCustomerPays() {
        // subtotal 48.00 is under the 50.00 ceiling; the customer pays 52.80.
        assertThat(Guard.authorize(ACTIVE,
                new OrderPricing(new BigDecimal("48.00"), new BigDecimal("4.80"))))
                .isNotNull();
    }

    @Test
    @DisplayName("a refusal names the amount and the cap, so the agent can relay it")
    void aRefusalNamesTheAmountAndTheCap() {
        String refusal = Guard.authorize(ACTIVE,
                new OrderPricing(new BigDecimal("65.00"), new BigDecimal("6.50")));
        assertThat(refusal).contains("71.5");  // what the customer would have paid
        assertThat(refusal).contains("50");    // the ceiling that refused it
    }

    @Test
    @DisplayName("a revoked mandate grants nothing")
    void aRevokedMandateGrantsNothing() {
        assertThat(Guard.authorize(REVOKED,
                new OrderPricing(new BigDecimal("5.00"), new BigDecimal("0.50"))))
                .isNotNull();
    }

    @Test
    @DisplayName("the ceiling holds under retry — same key, same order, one spend")
    void theCeilingHoldsUnderRetry() {
        BuyResult first = Shop.handleBuy(Shop.COURSE_MANDATE, 201, "retry-key-1");
        BuyResult again = Shop.handleBuy(Shop.COURSE_MANDATE, 201, "retry-key-1"); // agent retries

        assertThat(first).isInstanceOf(BuyResult.Completed.class);
        assertThat(again).isEqualTo(first);
        assertThat(Shop.ordersPlaced()).isEqualTo(1);
    }

    @Test
    @DisplayName("there is no approval tool for the agent to call — green before you start")
    void thereIsNoApprovalToolToCall() {
        var names = GuardedToolServer.tools().stream()
                .map(t -> t.name())
                .sorted()
                .toList();
        assertThat(names).containsExactly("buyTickets", "searchListings");
        assertThat(names).noneMatch(n -> n.toLowerCase().matches(".*(approve|override|raise).*"));
    }
}
