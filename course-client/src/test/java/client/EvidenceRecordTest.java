package client;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import client.Providers.ProviderListing;

import static org.assertj.core.api.Assertions.assertThat;

class EvidenceRecordTest {

    @Test
    @DisplayName("the evidence record answers 'why did you buy that?' with checkable fields")
    void evidenceAnswersWhy() {
        var profile = PurchaseProfile.of("Hamilton", "60.00", null);
        var decision = SourcingPolicy.decide(profile, Map.of(
                "TicketNexus", List.of(new ProviderListing("TicketNexus", 42, "Hamilton",
                        "Balcony", "C", "1", new BigDecimal("30.00"), new BigDecimal("3.00")))),
                Map.of());

        var evidence = EvidenceRecord.from(profile, decision);
        String rendered = evidence.render();

        assertThat(evidence.postconditionHeld()).isTrue();
        assertThat(rendered)
                .contains("TicketNexus")                                  // providers searched
                .contains(SourcingPolicy.POLICY_ID)                       // ranking policy, by name
                .contains("v1")                                           // profile version
                .contains("#42")                                          // the selected listing
                .contains("$33.00 all-in");                               // the customer's units
    }
}
