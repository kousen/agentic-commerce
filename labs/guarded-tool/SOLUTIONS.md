# Lab 2 solutions — instructor reference

Each guard is the same five decisions in its track's idiom: status first, total =
subtotal + fee (the customer's units), compare to the ceiling, refuse with both numbers,
otherwise authorize. All three verified green against the shipped tests.

## TypeScript — `typescript/src/guard.ts`

```typescript
export function authorize(mandate: Mandate, pricing: OrderPricing): string | null {
  if (mandate.status !== "ACTIVE") {
    return `No active mandate: ${mandate.mandateId} is ${mandate.status}`;
  }
  const charged = pricing.subtotal + pricing.serviceFee;
  if (charged > mandate.maxSpendPerTransaction) {
    return `Amount ${charged.toFixed(2)} exceeds the ${mandate.maxSpendPerTransaction.toFixed(2)} per-transaction cap on mandate ${mandate.mandateId}`;
  }
  return null;
}
```

## Python — `python/guard.py`

```python
def authorize(mandate: Mandate, pricing: OrderPricing) -> str | None:
    if mandate.status != "ACTIVE":
        return f"No active mandate: {mandate.mandate_id} is {mandate.status}"
    charged = pricing.subtotal + pricing.service_fee
    if charged > mandate.max_spend_per_transaction:
        return (f"Amount {charged} exceeds the {mandate.max_spend_per_transaction} "
                f"per-transaction cap on mandate {mandate.mandate_id}")
    return None
```

## Java — `java/src/main/java/lab/Guard.java`

```java
public static String authorize(Mandate mandate, OrderPricing pricing) {
    if (mandate.status() != Status.ACTIVE) {
        return "No active mandate: %s is %s".formatted(mandate.mandateId(), mandate.status());
    }
    var charged = pricing.subtotal().add(pricing.serviceFee());
    if (charged.compareTo(mandate.maxSpendPerTransaction()) > 0) {
        return "Amount %s exceeds the %s per-transaction cap on mandate %s"
                .formatted(charged, mandate.maxSpendPerTransaction(), mandate.mandateId());
    }
    return null;
}
```

## Teaching notes

- **The trap fires on the units check.** A student who writes
  `pricing.subtotal > ceiling` passes the in-mandate, revoked, and relay tests and
  fails only "the ceiling is denominated in what the customer pays." That failure is
  the §2.2 units lesson happening to them personally; call it out as a win, not a bug.
- **The refusal-message test is deliberately forgiving** — any phrasing containing the
  two numbers passes. The point is *relayability*, not format.
- **BigDecimal vs float:** the Java track uses `compareTo` on BigDecimal; if a student
  asks why not `==` or doubles, that's the money-arithmetic sidebar (30 seconds, no
  more).
