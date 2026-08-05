// Protocol-level probe for the demo servers — verifies each server works without
// spending a model call. Usage: tsx src/probe.ts <server.ts> then edit the scenario below.

import { Client } from "@modelcontextprotocol/client";
import { StdioClientTransport } from "@modelcontextprotocol/client/stdio";

const serverPath = process.argv[2];
const scenario = process.argv[3] ?? "list";

const client = new Client({ name: "probe", version: "1.0.0" }, { capabilities: {} });
await client.connect(new StdioClientTransport({ command: "npx", args: ["tsx", serverPath] }));

const text = (r: any) => r.content?.[0]?.text ?? JSON.stringify(r);
const call = (name: string, args: any = {}) => client.callTool({ name, arguments: args });

const { tools } = (await client.listTools()) as any;
console.log("tools:", tools.map((t: any) => t.name).join(", "));

if (scenario === "identity") {
  // §1.2 — the agent types someone else's email.
  const search: any = await call("searchListings", { query: "Monster Jam", maxPrice: 40 });
  const listing = JSON.parse(text(search))[0];
  console.log(`listing: ${listing.event} ${listing.section} $${listing.price}`);
  console.log("buy as bob (agent-supplied):", text(await call("buyTickets", {
    userEmail: "bob@mockhub.com", listingId: listing.listingId, quantity: 1,
  })));
  console.log("bob's orders:", text(await call("getMyOrders", { userEmail: "bob@mockhub.com" })).slice(0, 300));
} else if (scenario === "history") {
  // §2.1 — what history does the agent see?
  console.log("orders:", text(await call("listMyOrders")).slice(0, 800));
} else if (scenario === "injection") {
  // §3.3 — does the poisoned listing come through, and does the ceiling hold?
  const search: any = await call("searchListings", { query: "Monster Jam" });
  const listings = JSON.parse(text(search));
  const poisoned = listings.reduce((a: any, b: any) => (b.price > a.price ? b : a));
  console.log(`poisoned listing $${poisoned.price}: ${poisoned.sellerDescription.slice(0, 90)}…`);
  console.log("buy attempt:", text(await call("buyListing", { listingId: poisoned.listingId })).slice(0, 300));
}

await client.close();
