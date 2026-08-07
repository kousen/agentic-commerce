import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

/// The most transferable idea in the course:
/// let the model produce STRUCTURE — never let it produce AUTHORITY.
///
/// "Buy tickets like last time" is not an authorization; it is an invitation to infer
/// one. The inference must become an artifact: the LLM *proposes* a structured profile,
/// deterministic code validates it, the customer can inspect it, and the mandate
/// attaches to the profile — not to the raw utterance.
///
/// Run it:  java PurchaseProfileBoundary.java
public class PurchaseProfileBoundary {

    /// What the model is allowed to produce: a proposal. Note the type — every field is
    /// structured. There is no free-text field that downstream code might "interpret."
    record ProposedProfile(
            String eventQuery,
            String categorySlug,
            String preferredSection,
            BigDecimal maxTotalPrice,     // all-in, the customer's units
            String rationale) {           // shown to the customer; never evaluated by code
    }

    /// What validated structure becomes: an artifact with an identity, suitable for a
    /// mandate to attach to. Only validate() can mint one — the constructor is private
    /// to this file, and the model never touches this type.
    record PurchaseProfile(String profileId, ProposedProfile contents) { }

    sealed interface Validation {
        record Accepted(PurchaseProfile profile) implements Validation { }
        /// Rejections are specific enough to send back to the model for another proposal.
        record Rejected(List<String> problems) implements Validation { }
    }

    /// The schema speaks slugs; agents speak the customer's vocabulary. A field note
    /// from MockHub: an agent wrote allowedCategories: "jazz" — plausible, invalid, and
    /// it validated clean while blocking every purchase. Validate against the closed
    /// vocabulary and say what the legal values are.
    static final Set<String> KNOWN_CATEGORIES =
            Set.of("concerts", "sports", "theater", "comedy", "festivals");

    static Validation validate(ProposedProfile p) {
        var problems = new java.util.ArrayList<String>();

        // Models produce plausible values, not valid ones — and "" is not null.
        // (Second field note: an empty string meant "restricted to nothing" and
        // blocked every purchase while the mandate looked fine.)
        if (p.categorySlug() == null || p.categorySlug().isBlank()) {
            problems.add("categorySlug is missing or blank — omit the field entirely for no restriction");
        } else if (!KNOWN_CATEGORIES.contains(p.categorySlug())) {
            problems.add("unknown category '%s' — known categories: %s"
                    .formatted(p.categorySlug(), KNOWN_CATEGORIES));
        }
        if (p.eventQuery() == null || p.eventQuery().isBlank()) {
            problems.add("eventQuery is required");
        }
        if (p.maxTotalPrice() == null || p.maxTotalPrice().signum() <= 0) {
            problems.add("maxTotalPrice must be a positive amount in the customer's currency");
        }

        if (!problems.isEmpty()) return new Validation.Rejected(List.copyOf(problems));
        return new Validation.Accepted(
                new PurchaseProfile("profile-" + Math.abs(p.hashCode()), p));
    }

    // ---- self-check ---------------------------------------------------------------------

    public static void main(String[] args) {
        var good = new ProposedProfile("Monster Jam", "sports", "100 Level",
                new BigDecimal("50.00"), "Customer's history shows mid-tier seats around $50");
        check("a valid proposal becomes an inspectable artifact",
                validate(good) instanceof Validation.Accepted a
                        && a.profile().profileId() != null);

        var jazz = new ProposedProfile("Yo-Yo Ma", "jazz", null,
                new BigDecimal("110.00"), "Customer asked for jazz");
        check("the customer's vocabulary is rejected with the schema's legal values",
                validate(jazz) instanceof Validation.Rejected r
                        && r.problems().getFirst().contains("concerts"));

        var blank = new ProposedProfile("Hamilton", "", "Orchestra",
                new BigDecimal("60.00"), "");
        check("an empty string is caught — blank is not null, and it means the wrong thing",
                validate(blank) instanceof Validation.Rejected r
                        && r.problems().getFirst().contains("blank"));

        var freeLunch = new ProposedProfile("Hamilton", "theater", null,
                new BigDecimal("-1"), "");
        check("a nonsensical bound never becomes an artifact",
                validate(freeLunch) instanceof Validation.Rejected);

        System.out.println("OK — structure crossed the boundary; authority never did");
    }

    static void check(String sentence, boolean holds) {
        if (!holds) throw new AssertionError("FAILED: " + sentence);
        System.out.println("  ✓ " + sentence);
    }
}
