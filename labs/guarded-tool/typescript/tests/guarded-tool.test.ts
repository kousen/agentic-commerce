// LAB 2 — the tests are the contract. Run `npm test`: one passes already (the tool
// surface), five fail until you write the guard. The red sentences are your worklist.

import { describe, expect, test } from "vitest";
import { Client } from "@modelcontextprotocol/client";
import { StdioClientTransport } from "@modelcontextprotocol/client/stdio";
import { authorize, type Mandate } from "../src/guard.js";
import { handleBuy, ordersPlaced, COURSE_MANDATE } from "../src/shop.js";

const activeMandate: Mandate = { mandateId: "m-1", status: "ACTIVE", maxSpendPerTransaction: 50.0 };
const revokedMandate: Mandate = { mandateId: "m-2", status: "REVOKED", maxSpendPerTransaction: 50.0 };

describe("the guard", () => {
  test("an in-mandate purchase is authorized — autonomy is the success path", () => {
    expect(authorize(activeMandate, { subtotal: 28.0, serviceFee: 2.8 })).toBeNull();
  });

  test("the ceiling is denominated in what the customer pays, not the subtotal", () => {
    // subtotal 48.00 is under the 50.00 ceiling; the customer pays 52.80.
    const refusal = authorize(activeMandate, { subtotal: 48.0, serviceFee: 4.8 });
    expect(refusal).not.toBeNull();
  });

  test("a refusal names the amount and the cap, so the agent can relay it", () => {
    const refusal = authorize(activeMandate, { subtotal: 65.0, serviceFee: 6.5 });
    expect(refusal).toMatch(/71\.5/);  // what the customer would have paid
    expect(refusal).toMatch(/50/);     // the ceiling that refused it
  });

  test("a revoked mandate grants nothing", () => {
    expect(authorize(revokedMandate, { subtotal: 5.0, serviceFee: 0.5 })).not.toBeNull();
  });
});

describe("the tool behind the guard", () => {
  test("the ceiling holds under retry — same key, same order, one spend", () => {
    const first = handleBuy(COURSE_MANDATE, 201, "retry-key-1");
    const again = handleBuy(COURSE_MANDATE, 201, "retry-key-1"); // lost response, agent retries
    expect(first.outcome).toBe("COMPLETED");
    expect(again).toEqual(first);
    expect(ordersPlaced()).toBe(1);
  });
});

describe("the tool surface (green before you start — this one is the scaffold's promise)", () => {
  test("there is no approval tool for the agent to call", async () => {
    const client = new Client({ name: "lab-test", version: "1.0.0" }, { capabilities: {} });
    await client.connect(
      new StdioClientTransport({ command: "npx", args: ["tsx", "src/server.ts"] }),
    );
    try {
      const { tools } = await client.listTools();
      const names = tools.map((t) => t.name).sort();
      expect(names).toEqual(["buyTickets", "searchListings"]);
      expect(names.some((n) => /approve|override|raise/i.test(n))).toBe(false);
    } finally {
      await client.close();
    }
  }, 30_000);
});
