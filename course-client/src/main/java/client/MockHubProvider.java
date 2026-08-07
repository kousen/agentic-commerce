package client;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import client.Providers.ProviderListing;
import client.Providers.TicketProvider;

/// A ticket provider backed by MockHub's ACP listings endpoint.
///
/// Two instances of this class with different names play the two-provider role from
/// the course demos (TicketNexus / SeatStream): identical inventory, so the sourcing
/// policy's cross-provider search and deduplication are exercised for real.
public final class MockHubProvider implements TicketProvider {

    /// MockHub applies a 10% service fee at checkout (OrderPricing, backend).
    /// The listing feed carries the subtotal; the policy must rank by what the
    /// customer will actually pay, so the fee is computed here, the same way.
    static final BigDecimal SERVICE_FEE_RATE = new BigDecimal("0.10");

    private static final HttpClient HTTP = HttpClient.newHttpClient();
    private static final ObjectMapper JSON = new ObjectMapper();

    private final String name;
    private final String baseUrl;
    private final String apiKey;

    public MockHubProvider(String name, String baseUrl, String apiKey) {
        this.name = name;
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public List<ProviderListing> search(PurchaseProfile profile) throws Exception {
        // maxPrice filters on the listing subtotal; every listing with an in-budget
        // all-in total has an in-budget subtotal, so this is a safe superset. The
        // policy applies the real (all-in) bound afterwards.
        String url = "%s/acp/v1/listings?query=%s&maxPrice=%s&size=50".formatted(
                baseUrl, URLEncoder.encode(profile.eventQuery(), StandardCharsets.UTF_8),
                profile.maxTotalPrice());
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .header("X-API-Key", apiKey)
                .GET()
                .build();
        HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IllegalStateException(name + " answered " + response.statusCode());
        }

        var listings = new ArrayList<ProviderListing>();
        for (JsonNode node : JSON.readTree(response.body()).path("content")) {
            BigDecimal subtotal = new BigDecimal(node.path("price").asText());
            BigDecimal fee = subtotal.multiply(SERVICE_FEE_RATE).setScale(2, RoundingMode.HALF_UP);
            listings.add(new ProviderListing(
                    name,
                    node.path("listingId").asLong(),
                    node.path("eventName").asText(),
                    node.path("sectionName").asText(),
                    node.path("rowLabel").asText(),
                    node.path("seatNumber").asText(),
                    subtotal,
                    fee));
        }
        return listings;
    }
}
