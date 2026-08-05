package lab;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

/**
 * Shared plumbing for the lab tests. Students should not need to edit this file.
 */
public final class Lab {
    public static final String BASE_URL = env("MOCKHUB_URL", "https://mockhub.kousenit.com");
    public static final String ACP_KEY = env("MOCKHUB_ACP_KEY", "mockhub-dev-key");
    public static final String EMAIL = env("MOCKHUB_EMAIL", "buyer@mockhub.com");
    public static final String PASSWORD = env("MOCKHUB_PASSWORD", "buyer123");

    private static final HttpClient CLIENT = HttpClient.newHttpClient();
    private static final ObjectMapper JSON = new ObjectMapper();

    private Lab() {
    }

    private static String env(String name, String fallback) {
        return System.getenv().getOrDefault(name, fallback);
    }

    /** The HUMAN's credential: a JWT from the buyer's username and password. */
    public static String login() {
        var response = post("/api/v1/auth/login",
                """
                {"email": "%s", "password": "%s"}""".formatted(EMAIL, PASSWORD));
        return json(response).get("accessToken").asText();
    }

    /** Unique per run, so students sharing the demo account never collide. */
    public static String freshAgentId() {
        return "lab-agent-" + UUID.randomUUID().toString().substring(0, 12);
    }

    public static JsonNode createMandate(String token, String agentId,
                                         String maxSpendPerTransaction, String approvalMode) {
        var response = post("/api/v1/my/mandates",
                """
                {"agentId": "%s", "scope": "PURCHASE",
                 "maxSpendPerTransaction": %s, "approvalMode": "%s"}"""
                        .formatted(agentId, maxSpendPerTransaction, approvalMode),
                "Authorization", "Bearer " + token);
        return json(response);
    }

    public static void revokeMandate(String token, String mandateId) {
        send(request("/api/v1/my/mandates/" + mandateId, "Authorization", "Bearer " + token)
                .DELETE().build());
    }

    /** Any listing costing more than a dollar; a random page avoids seat contention. */
    public static JsonNode pickAListing() {
        int page = new java.util.Random().nextInt(5);
        var response = send(request(
                "/acp/v1/listings?size=10&minPrice=10&page=" + page,
                "X-API-Key", ACP_KEY).GET().build());
        var listings = json(response).get("content");
        if (listings == null || listings.isEmpty()) {
            throw new AssertionError("MockHub should have listings for sale");
        }
        return listings.get(new java.util.Random().nextInt(listings.size()));
    }

    /** The agent's purchase path: ACP checkout, carrying its mandate. */
    public static HttpResponse<String> agentTriesToBuy(String agentId, String mandateId,
                                                       long listingId) {
        return post("/acp/v1/checkout",
                """
                {"buyerEmail": "%s",
                 "lineItems": [{"listingId": %d, "quantity": 1}],
                 "agentId": "%s", "mandateId": "%s"}"""
                        .formatted(EMAIL, listingId, agentId, mandateId),
                "X-API-Key", ACP_KEY);
    }

    public static HttpResponse<String> agentTriesToComplete(String checkoutId, String agentId,
                                                            String mandateId) {
        return post("/acp/v1/checkout/" + checkoutId + "/complete",
                """
                {"agentId": "%s", "mandateId": "%s"}""".formatted(agentId, mandateId),
                "X-API-Key", ACP_KEY, "X-Buyer-Email", EMAIL);
    }

    public static void cancelCheckout(String checkoutId) {
        post("/acp/v1/checkout/" + checkoutId + "/cancel", "{}",
                "X-API-Key", ACP_KEY, "X-Buyer-Email", EMAIL);
    }

    // -- HTTP plumbing --------------------------------------------------------

    public static HttpResponse<String> get(String path) {
        return send(request(path).GET().build());
    }

    public static HttpResponse<String> post(String path, String body, String... headers) {
        return send(request(path, headers)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build());
    }

    public static JsonNode json(HttpResponse<String> response) {
        try {
            return JSON.readTree(response.body());
        } catch (Exception e) {
            throw new AssertionError("Expected JSON but got: " + response.body(), e);
        }
    }

    private static HttpRequest.Builder request(String path, String... headers) {
        var builder = HttpRequest.newBuilder(URI.create(BASE_URL + path));
        for (int i = 0; i < headers.length; i += 2) {
            builder.header(headers[i], headers[i + 1]);
        }
        return builder;
    }

    private static HttpResponse<String> send(HttpRequest request) {
        try {
            return CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new AssertionError("Could not reach MockHub at " + BASE_URL
                    + " — check your network, then ask in chat. (" + e.getMessage() + ")", e);
        }
    }
}
