package lab;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * LAB — The mandate boundary test.
 *
 * Each test states a sentence of the contract between a customer and their buying agent,
 * then proves the platform enforces it. Read the display names out loud: they are the lesson.
 *
 * Design by contract:
 *   precondition   — a mandate exists, and it is the ONLY authority the agent holds
 *   postcondition  — every purchase inside the mandate can succeed; everything outside is refused
 *   invariant      — nothing the agent can do changes the mandate's boundaries
 */
class MandateBoundaryLabTest {

    static String token;

    @BeforeAll
    static void theHumanLogsIn() {
        token = Lab.login();
    }

    @Test
    @DisplayName("The mandate ceiling holds — a purchase above the limit is refused, not negotiated")
    void theMandateCeilingHolds() {
        var agentId = Lab.freshAgentId();
        var aDollarMandate = Lab.createMandate(token, agentId, "1.00", "AUTO_PURCHASE");
        var somethingPricier = Lab.pickAListing();

        var response = Lab.agentTriesToBuy(agentId,
                aDollarMandate.get("mandateId").asText(),
                somethingPricier.get("listingId").asLong());

        assertThat(response.statusCode()).as("the purchase should be refused").isEqualTo(409);
        assertThat(response.body()).contains("Mandate does not authorize");

        Lab.revokeMandate(token, aDollarMandate.get("mandateId").asText());
    }

    @Test
    @DisplayName("A revoked mandate grants nothing — yesterday's authority is not today's")
    void aRevokedMandateGrantsNothing() {
        var agentId = Lab.freshAgentId();
        var mandate = Lab.createMandate(token, agentId, "500.00", "AUTO_PURCHASE");
        Lab.revokeMandate(token, mandate.get("mandateId").asText());

        var response = Lab.agentTriesToBuy(agentId,
                mandate.get("mandateId").asText(),
                Lab.pickAListing().get("listingId").asLong());

        assertThat(response.statusCode()).as("the purchase should be refused").isEqualTo(409);
        assertThat(response.body()).contains("No active mandate");
    }

    @Test
    @DisplayName("A purchase needing approval cannot complete without one — no 'no' is not a 'yes'")
    void aPurchaseNeedingApprovalCannotCompleteWithoutOne() {
        var agentId = Lab.freshAgentId();
        var mandate = Lab.createMandate(token, agentId, "500.00", "APPROVAL_REQUIRED");
        var mandateId = mandate.get("mandateId").asText();

        // A listing can be momentarily held by a classmate's run; try a few.
        HttpResponse<String> checkout = null;
        for (int attempt = 0; attempt < 3; attempt++) {
            checkout = Lab.agentTriesToBuy(agentId, mandateId,
                    Lab.pickAListing().get("listingId").asLong());
            if (checkout.statusCode() == 201) break;
        }
        assertThat(checkout.statusCode()).as("staging the checkout is allowed").isEqualTo(201);
        var checkoutId = Lab.json(checkout).get("checkoutId").asText();

        var response = Lab.agentTriesToComplete(checkoutId, agentId, mandateId);

        assertThat(response.statusCode()).as("completion should be refused").isEqualTo(409);
        assertThat(response.body()).contains("requires an approved purchase approval");

        Lab.cancelCheckout(checkoutId, agentId, mandateId);
        Lab.revokeMandate(token, mandateId);
    }

    @Test
    @DisplayName("The agent's credential cannot approve a purchase — an approval the agent can invoke is not an approval")
    void theAgentCredentialCannotApproveAPurchase() {
        // The approve endpoint exists — but it accepts only the human's login, never the
        // agent's API key. Same action, different door, and the agent's key does not fit.
        var response = Lab.post("/api/v1/agent-approvals/any-proposal/approve", "{}",
                "X-API-Key", Lab.ACP_KEY);  // the AGENT's credential — deliberately not a JWT

        assertThat(response.statusCode())
                .as("the agent's credential should not open this door")
                .isEqualTo(401);
    }

    @Test
    @Disabled("YOUR TURN — delete this line and write the test (see labs.md)")
    @DisplayName("The agent's credential cannot mint a mandate — authority comes from artifacts the model cannot mint")
    void theAgentCredentialCannotMintAMandate() {
        // Prove it: try to CREATE a mandate (POST /api/v1/my/mandates) using only the
        // agent's credential (X-API-Key), and assert the platform refuses with 401.
        // The test above is the pattern; this is the same idea one level deeper.
        throw new UnsupportedOperationException("write me");
    }
}
