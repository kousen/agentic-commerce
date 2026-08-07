# The mandate check

*Front Door step 4. Checklist lines: "boundaries evaluate in deterministic code" and
"denominate the bound in the units the customer meant."*

```bash
java MandateCheck.java
```

One function, `authorize(mandate, pricing, category, now)`, that answers the only
question that matters before money moves: **does the artifact the customer signed cover
this exact charge, right now?** No model is consulted. The model proposed the purchase;
it does not get a vote on authorization.

What the file demonstrates, each as a runnable assertion:

- **The units lesson** (learned from two real orders during the course build): the
  ceiling evaluates the **all-in total**, assembled in one `OrderPricing` record — never
  the subtotal. A $35 ceiling that validates $32.15 + $3.22 in fees authorized a $35.37
  charge until MockHub fixed exactly this.
- **Checked at spend time, not issue time** — status and expiry are evaluated at the
  moment of the charge, so revocation is immediate and "yesterday's authority is not
  today's."
- **Refusals are relayable** — the reason names the amount, the cap, and the mandate.
  The agent will retry regardless; tell it the constraint or it will invent one.
- **Both axes hold** — per-transaction cap and cumulative budget are independent checks.

## In MockHub

The production version lives in `AcpCheckoutService` (validation on create, update, and
— since 2026-08-05 — re-authorization on complete) with `OrderPricing` as the single
place the fee is applied, and `MandateCondition` running the same check on the MCP tool
path. Spend accounting (recorded on confirmation, reversed on cancellation, row-locked)
is the part this excerpt stubs as a field — see `code-tour.md` for why that's a real
accounting problem.
