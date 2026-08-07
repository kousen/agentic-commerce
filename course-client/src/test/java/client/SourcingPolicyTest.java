package client;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import client.Providers.ProviderListing;
import client.Providers.TicketProvider;
import client.SourcingPolicy.SourcingDecision;

import static org.assertj.core.api.Assertions.assertThat;

/// The sourcing contract, one sentence per test. Fixtures only — no network.
class SourcingPolicyTest {

    static ProviderListing listing(String provider, long id, String section, String row,
                                   String seat, String subtotal, String fee) {
        return new ProviderListing(provider, id, "Hamilton", section, row, seat,
                new BigDecimal(subtotal), new BigDecimal(fee));
    }

    static final PurchaseProfile UNDER_60_ANY_SECTION = PurchaseProfile.of("Hamilton", "60.00", null);
    static final PurchaseProfile UNDER_60_ORCHESTRA = PurchaseProfile.of("Hamilton", "60.00", "Orchestra");

    @Test
    @DisplayName("the minimum-cost listing wins across ALL searched providers, not per provider")
    void minimumCostAcrossAllProviders() {
        var decision = SourcingPolicy.decide(UNDER_60_ANY_SECTION, Map.of(
                "TicketNexus", List.of(listing("TicketNexus", 1, "Balcony", "C", "1", "40.00", "4.00")),
                "SeatStream", List.of(listing("SeatStream", 2, "Balcony", "D", "5", "30.00", "3.00"))),
                Map.of());

        assertThat(decision.selected().listingId()).isEqualTo(2);
        assertThat(decision.providersSearched()).containsExactly("TicketNexus", "SeatStream");
        assertThat(SourcingPolicy.postconditionHolds(UNDER_60_ANY_SECTION, decision)).isTrue();
    }

    @Test
    @DisplayName("eligibility is judged in the customer's units — all-in total, not subtotal")
    void allInTotalDecidesEligibility() {
        // subtotal 58 is under the 60 budget; the customer would pay 63.80.
        var decision = SourcingPolicy.decide(UNDER_60_ANY_SECTION, Map.of(
                "TicketNexus", List.of(listing("TicketNexus", 1, "Balcony", "C", "1", "58.00", "5.80"))),
                Map.of());

        assertThat(decision.selected()).isNull();
        assertThat(decision.exceptionRecord()).contains("No listing satisfied the profile");
        assertThat(SourcingPolicy.postconditionHolds(UNDER_60_ANY_SECTION, decision)).isTrue();
    }

    @Test
    @DisplayName("a declared section preference outranks raw price — with the reason disclosed")
    void declaredPreferenceOutranksPrice() {
        var decision = SourcingPolicy.decide(UNDER_60_ORCHESTRA, Map.of(
                "TicketNexus", List.of(
                        listing("TicketNexus", 1, "Balcony", "C", "1", "30.00", "3.00"),
                        listing("TicketNexus", 2, "Orchestra", "A", "7", "50.00", "5.00"))),
                Map.of());

        assertThat(decision.selected().listingId()).isEqualTo(2);
        assertThat(decision.selectionReasons()).contains("MATCHES_PREFERRED_SECTION");
        assertThat(SourcingPolicy.postconditionHolds(UNDER_60_ORCHESTRA, decision)).isTrue();
    }

    @Test
    @DisplayName("an unmet preference becomes an exception record, not a silent fallback")
    void unmetPreferenceIsDisclosed() {
        var decision = SourcingPolicy.decide(UNDER_60_ORCHESTRA, Map.of(
                "TicketNexus", List.of(listing("TicketNexus", 1, "Balcony", "C", "1", "30.00", "3.00"))),
                Map.of());

        assertThat(decision.selected().listingId()).isEqualTo(1);
        assertThat(decision.exceptionRecord()).contains("preferred section 'Orchestra'");
        assertThat(SourcingPolicy.postconditionHolds(UNDER_60_ORCHESTRA, decision)).isTrue();
    }

    @Test
    @DisplayName("the same physical seat from two providers is one candidate — the cheaper offer")
    void duplicateSeatsAreMerged() {
        var decision = SourcingPolicy.decide(UNDER_60_ANY_SECTION, Map.of(
                "TicketNexus", List.of(listing("TicketNexus", 1, "Balcony", "C", "1", "35.00", "3.50")),
                "SeatStream", List.of(listing("SeatStream", 2, "Balcony", "C", "1", "33.00", "3.30"))),
                Map.of());

        assertThat(decision.candidates()).hasSize(1);
        assertThat(decision.selected().provider()).isEqualTo("SeatStream");
    }

    @Test
    @DisplayName("a provider failure is disclosed in the exception record — never silently skipped")
    void providerFailureIsDisclosed() {
        var decision = SourcingPolicy.decide(UNDER_60_ANY_SECTION, Map.of(
                "TicketNexus", List.of(listing("TicketNexus", 1, "Balcony", "C", "1", "30.00", "3.00"))),
                Map.of("SeatStream", "connection refused"));

        assertThat(decision.selected()).isNotNull();
        assertThat(decision.exceptionRecord()).contains("SeatStream");
        assertThat(decision.providerFailures()).containsEntry("SeatStream", "connection refused");
    }

    @Test
    @DisplayName("nothing eligible anywhere still produces a decision — with the reason on the record")
    void emptyMarketIsAnAnsweredQuestion() {
        var decision = SourcingPolicy.decide(UNDER_60_ANY_SECTION,
                Map.of("TicketNexus", List.of(), "SeatStream", List.of()), Map.of());

        assertThat(decision.selected()).isNull();
        assertThat(decision.exceptionRecord()).isNotNull();
        assertThat(SourcingPolicy.postconditionHolds(UNDER_60_ANY_SECTION, decision)).isTrue();
    }
}
