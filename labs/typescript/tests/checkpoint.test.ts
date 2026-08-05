// The setup checkpoint, run at 00:15:  npm run checkpoint
// Paste the CHECKPOINT line it prints into the class chat.

import { expect, test } from "vitest";
import { BASE_URL, get } from "./lab";

test("checkpoint: MockHub is reachable and healthy", async () => {
  const response = await get("/actuator/health");

  expect(response.status, "MockHub health endpoint should answer").toBe(200);
  const body = await response.json();
  expect(body.status, "MockHub should report itself UP").toBe("UP");

  console.log(`CHECKPOINT OK — typescript — ${BASE_URL}`);
});
