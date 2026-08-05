// §3.1 demo, provider A: TicketNexus — the well-documented provider.
// Same MockHub inventory as SeatStream; only the tool surface differs.

import { McpServer } from "@modelcontextprotocol/server";
import { serveStdio } from "@modelcontextprotocol/server/stdio";
import * as z from "zod/v4";
import { acpGet, asText, compactListing } from "./mockhub.js";

serveStdio(() => {
  const server = new McpServer({ name: "ticketnexus", version: "1.0.0" });

  server.registerTool(
    "searchEvents",
    {
      description:
        "Search live-event inventory by artist, event name, venue, or city. " +
        "Returns matching events with dates, venues, and cities. " +
        "Use this first to find an event, then getEventListings for tickets.",
      inputSchema: z.object({
        query: z.string().describe("Artist, event, or venue name"),
        city: z.string().optional().describe("Filter by city"),
      }),
    },
    async ({ query, city }) => {
      const params = new URLSearchParams({ query, size: "8" });
      if (city) params.set("city", city);
      const page = await acpGet(`/acp/v1/catalog?${params}`);
      return asText(
        page.content.map((e: any) => ({
          event: e.name,
          slug: e.productId,
          date: e.eventDate,
          venue: e.venue,
          city: e.city,
          priceRange: `$${e.minPrice}–$${e.maxPrice}`,
        })),
      );
    },
  );

  server.registerTool(
    "getEventListings",
    {
      description:
        "List available ticket listings for an event, with section, row, seat, " +
        "and price. Supports an optional price range in dollars.",
      inputSchema: z.object({
        query: z.string().describe("Event or artist name to match listings against"),
        minPrice: z.number().optional(),
        maxPrice: z.number().optional(),
      }),
    },
    async ({ query, minPrice, maxPrice }) => {
      const params = new URLSearchParams({ query, size: "10" });
      if (minPrice != null) params.set("minPrice", String(minPrice));
      if (maxPrice != null) params.set("maxPrice", String(maxPrice));
      const page = await acpGet(`/acp/v1/listings?${params}`);
      return asText(page.content.map(compactListing));
    },
  );

  return server;
});
