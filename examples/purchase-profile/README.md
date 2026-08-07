# The PurchaseProfile boundary

*Module 2, §2.3. Checklist line: "let the model produce structure; never let it produce
authority."*

```bash
java PurchaseProfileBoundary.java
```

"Buy tickets like last time" carries no authorization — it invites the agent to infer
one. In the live demo the agent anchored on a *Hamilton* Floor seat and picked Floor at
Monster Jam: faithful to the data, inside the mandate, wrong. The fix is a boundary with
a type on each side:

- **`ProposedProfile`** — what the model may produce. Every field structured; the
  `rationale` is shown to the customer but never evaluated by code.
- **`PurchaseProfile`** — what validated structure becomes: an artifact with an
  identity, which the mandate attaches to. Only `validate()` can mint one.

The validation encodes two real field notes from letting an agent loose on MockHub:

1. **Agents speak the customer's vocabulary; your schema speaks slugs.** An agent wrote
   `allowedCategories: "jazz"` — plausible, invalid, validated clean, blocked every
   purchase. The rejection message lists the legal values so the model's retry can
   succeed.
2. **Models produce plausible values, not valid ones.** An empty string is not null:
   `""` meant "restricted to nothing" and silently disabled a mandate. Blank-vs-null is
   a first-class check.

Rejections return *specific problems*, because the loop is: model proposes → code
rejects with reasons → model proposes again → customer inspects the accepted artifact.
The model is a proposal generator; the artifact is what authority attaches to.
