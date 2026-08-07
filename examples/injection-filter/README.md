# The free-text boundary

*Module 3, §3.2. Checklist line: "free text never widens a mandate."*

```bash
java FreeTextBoundary.java
```

The name "injection filter" is half a joke: **there is no filter.** Filters hunt for
hostile phrases and lose eventually. The defense here is structural — the authorization
decision's *type signature* only admits structured fields (`totalCharged`,
`categorySlug`), so seller-written prose has no path into the decision. The compiler
enforces the rule, not a regex. One conversion function is the only way a listing
becomes an authorization question, and it drops the description on the floor.

The poisoned listing text in the self-check is the real one from the course demo server
(`demos/src/injected-provider.ts`): forged "account metadata" claiming the customer
pre-authorized $75. In ten recorded runs across frontier and small models, **zero
agents obeyed it** — several passed on the expensive listing out loud. The models can
read the prose; the boundary cannot, and that asymmetry is the design.

Why write the boundary after ten clean runs: the seller only needs it to work once, on
one model version, and the boundary costs nothing. The same runs produced the *units
overrun* (two orders over the customer's stated $35 with no attacker involved), which
is why this example's refusal is denominated in the fee-inclusive total — the two legs
of the §3.2 contract share one fix: **authorization decisions happen in code, on
structured values, in the customer's units.**
