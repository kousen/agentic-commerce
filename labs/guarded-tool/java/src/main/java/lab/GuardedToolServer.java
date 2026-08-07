package lab;

import java.util.List;
import java.util.Map;

import io.modelcontextprotocol.json.McpJsonDefaults;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.ServerCapabilities;
import io.modelcontextprotocol.spec.McpSchema.Tool;

import lab.Shop.BuyResult;

/// Scaffolding — the MCP wiring. Two tools, over stdio, using the official SDK.
///
/// Count the tools. There is no approvePurchase, no raiseCeiling, no overrideMandate.
/// The boundary is not something the agent negotiates with; it's the shape of its world.
///
/// Run it yourself:            ./gradlew run
/// Connect a real agent to it: claude --mcp-config mcp-config.json --strict-mcp-config
public final class GuardedToolServer {

    /// The complete tool surface — the single source of truth for the server AND the
    /// test that asserts no approval tool exists.
    public static List<Tool> tools() {
        return List.of(
                Tool.builder("searchListings", Map.of("type", "object", "properties", Map.of()))
                        .description("List the available ticket listings with all-in pricing "
                                + "(subtotal plus service fee).")
                        .build(),
                Tool.builder("buyTickets", Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "listingId", Map.of("type", "integer"),
                                        "idempotencyKey", Map.of("type", "string")),
                                "required", List.of("listingId", "idempotencyKey")))
                        .description("Buy one listing on the customer's behalf under their mandate. "
                                + "Refusals state the constraint. Pass a fresh idempotencyKey per "
                                + "purchase; reuse the SAME key when retrying.")
                        .build());
    }

    public static void main(String[] args) {
        List<Tool> tools = tools();
        McpSyncServer server = McpServer.sync(new StdioServerTransportProvider(McpJsonDefaults.getMapper()))
                .serverInfo("guarded-ticket-shop", "1.0.0")
                .capabilities(ServerCapabilities.builder().tools(true).build())
                .toolCall(tools.get(0), (exchange, request) -> text(listingsJson()))
                .toolCall(tools.get(1), (exchange, request) -> {
                    long listingId = ((Number) request.arguments().get("listingId")).longValue();
                    String key = String.valueOf(request.arguments().get("idempotencyKey"));
                    return text(resultJson(Shop.handleBuy(Shop.COURSE_MANDATE, listingId, key)));
                })
                .build();
        // stdio transport: the server now serves until the client disconnects.
    }

    private static CallToolResult text(String body) {
        return CallToolResult.builder()
                .content(List.of(McpSchema.TextContent.builder(body).build()))
                .build();
    }

    private static String listingsJson() {
        var rows = Shop.LISTINGS.stream()
                .map(l -> """
                        {"listingId": %d, "event": "%s", "section": "%s", \
                        "subtotal": %s, "serviceFee": %s, "description": "%s"}"""
                        .formatted(l.listingId(), l.event(), l.section(),
                                l.subtotal(), l.serviceFee(), l.description().replace("\"", "'")))
                .toList();
        return "[" + String.join(",\n ", rows) + "]";
    }

    private static String resultJson(BuyResult result) {
        return switch (result) {
            case BuyResult.Completed c -> """
                    {"outcome": "COMPLETED", "orderNumber": "%s", "totalCharged": %s}"""
                    .formatted(c.orderNumber(), c.totalCharged());
            case BuyResult.Refused r -> """
                    {"outcome": "REFUSED", "reason": "%s"}"""
                    .formatted(r.reason().replace("\"", "'"));
        };
    }

    private GuardedToolServer() { }
}
