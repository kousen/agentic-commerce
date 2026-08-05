// Protocol-level test for the guarded server's multi-round-trip elicitation.
// Usage: tsx src/test-guarded-client.ts [approve|decline]
// Plays the HOST role: the elicitation handler is where the human would answer.

import { Client } from "@modelcontextprotocol/client";
import { StdioClientTransport } from "@modelcontextprotocol/client/stdio";

const humanSaysYes = process.argv[2] === "approve";

const client = new Client(
  { name: "demo-host", version: "1.0.0" },
  { capabilities: { elicitation: { form: {} } } },
);

let elicitationSeen = false;
client.setRequestHandler("elicitation/create", async (request: any) => {
  elicitationSeen = true;
  console.log(`\n[HOST UI] ${request.params.message}`);
  console.log(`[HUMAN] answers: ${humanSaysYes ? "approve" : "decline"}\n`);
  return { action: "accept", content: { approve: humanSaysYes } };
});

await client.connect(
  new StdioClientTransport({ command: "npx", args: ["tsx", "src/guarded-tools.ts"] }),
);

const search: any = await client.callTool({
  name: "searchListings",
  arguments: { query: "Hamilton", maxPrice: 60 },
});
const options = JSON.parse(search.content[0].text);
const listing = options[Math.floor(Math.random() * options.length)];
console.log(`agent picked: ${listing.event} ${listing.section} row ${listing.row} — $${listing.price}`);

const result: any = await client.callTool({
  name: "buyTickets",
  arguments: { listingId: listing.listingId, expectedPrice: listing.price },
});

console.log("final result:", result.content?.[0]?.text ?? JSON.stringify(result));
console.log("elicitation went through the host:", elicitationSeen);
await client.close();
