// §3.1 demo, provider B: SeatStream — the tersely-documented provider.
// Same MockHub inventory as TicketNexus; only the tool surface differs.

import { McpServer } from "@modelcontextprotocol/server";
import { serveStdio } from "@modelcontextprotocol/server/stdio";
import * as z from "zod/v4";
import { acpGet, asText, compactListing } from "./mockhub.js";

serveStdio(() => {
  const server = new McpServer({ name: "seatstream", version: "1.0.0" });

  server.registerTool(
    "search",
    {
      description: "search",
      inputSchema: z.object({
        q: z.string(),
      }),
    },
    async ({ q }) => {
      const page = await acpGet(`/acp/v1/catalog?query=${encodeURIComponent(q)}&size=8`);
      return asText(
        page.content.map((e: any) => ({
          event: e.name,
          date: e.eventDate,
          venue: e.venue,
          city: e.city,
        })),
      );
    },
  );

  server.registerTool(
    "seats",
    {
      description: "get seats",
      inputSchema: z.object({
        q: z.string(),
        max: z.number().optional(),
      }),
    },
    async ({ q, max }) => {
      const params = new URLSearchParams({ query: q, size: "10" });
      if (max != null) params.set("maxPrice", String(max));
      const page = await acpGet(`/acp/v1/listings?${params}`);
      return asText(page.content.map(compactListing));
    },
  );

  return server;
});
