// §2.1 demo: "buy tickets like last time."
//
// Read-only on purpose. The demo watches the agent turn prior orders into an UNAUDITED
// inference about what the customer wants. The buyer's real history is genuinely
// ambiguous — $92 Foo Fighters 100 Level, $61 Green Day balcony, $106 Yo-Yo Ma
// orchestra, $31 Hamilton — so "like last time" has no single meaning, and whatever
// the agent picks it will state as if it were the customer's preference.
//
// The purchase would be fully inside the mandate and possibly still wrong.
// The fix (§2.2) is making the inference an artifact the customer can inspect.

import { McpServer } from "@modelcontextprotocol/server";
import { serveStdio } from "@modelcontextprotocol/server/stdio";
import * as z from "zod/v4";
import { acpGet, asText, compactListing, login, userGet } from "./mockhub.js";

const token = await login();

/** Confirmed orders only, with seat-level detail — the history a real agent would read. */
async function purchaseHistory(limit = 6) {
  const page = await userGet(token, "/api/v1/orders?size=30");
  const confirmed = (page.content ?? []).filter((o: any) => o.status === "CONFIRMED").slice(0, limit);
  return Promise.all(
    confirmed.map(async (summary: any) => {
      const order = await userGet(token, `/api/v1/orders/${summary.orderNumber}`);
      return {
        orderNumber: order.orderNumber,
        purchasedOn: order.createdAt,
        total: order.total,
        seats: (order.items ?? []).map((i: any) => ({
          event: i.eventName,
          section: i.sectionName,
          row: i.rowLabel,
          seat: i.seatNumber,
          pricePaid: i.pricePaid,
        })),
      };
    }),
  );
}

serveStdio(() => {
  const server = new McpServer({ name: "tickethub-history", version: "1.0.0" });

  server.registerTool(
    "listMyOrders",
    {
      description:
        "List the customer's past confirmed ticket orders, most recent first, " +
        "including the section, row, seat, and price paid for each.",
      inputSchema: z.object({}),
    },
    async () => asText(await purchaseHistory()),
  );

  server.registerTool(
    "searchListings",
    {
      description:
        "Search current ticket listings by event or artist name. Returns section, row, " +
        "seat, and price for each available listing.",
      inputSchema: z.object({
        query: z.string(),
        maxPrice: z.number().optional().describe("Maximum price per ticket, in dollars"),
      }),
    },
    async ({ query, maxPrice }) => {
      // Filter on price here: MockHub's maxPrice also excludes at the event level.
      const page = await acpGet(`/acp/v1/listings?query=${encodeURIComponent(query)}&size=60`);
      const listings = page.content
        .map(compactListing)
        .filter((l: any) => maxPrice == null || l.price <= maxPrice);
      return asText(listings.slice(0, 12));
    },
  );

  return server;
});
