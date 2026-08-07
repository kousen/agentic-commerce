package client;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import client.Providers.ProviderListing;
import client.Providers.TicketProvider;

/// Module 3, §3.4 — sourcing as a contract, implemented.
///
/// The protocol gives an agent reach; it says nothing about how to act as a
/// trustworthy buyer's representative. That behavior is a policy the application
/// supplies — this class — and it is deliberately NOT an LLM. Judgment the customer
/// can audit beats judgment the model improvises.
///
/// The postcondition (checkable, not narrative):
///   the selected listing is the minimum-cost listing satisfying the profile across
///   all searched providers — or an exception record explains why not.
public final class SourcingPolicy {

    public static final String POLICY_ID = "min-total-cost-with-declared-preferences/v1";

    public record RankedListing(ProviderListing listing, List<String> reasonCodes) { }

    public record SourcingDecision(
            ProviderListing selected,            // null when nothing satisfied the profile
            List<RankedListing> candidates,      // every listing considered, ranked
            List<String> providersSearched,
            Map<String, String> providerFailures,
            List<String> selectionReasons,
            String exceptionRecord) {            // non-null whenever the simple rule didn't apply
    }

    /// Step 1 of the contract: search EVERY eligible provider. A provider failure is
    /// recorded and disclosed — never silently skipped, because "the best option" from
    /// half the market is not the best option.
    public static SourcingDecision source(PurchaseProfile profile, List<TicketProvider> providers) {
        var results = new LinkedHashMap<String, List<ProviderListing>>();
        var failures = new LinkedHashMap<String, String>();
        for (TicketProvider provider : providers) {
            try {
                results.put(provider.name(), provider.search(profile));
            } catch (Exception e) {
                failures.put(provider.name(), e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage());
            }
        }
        return decide(profile, results, failures);
    }

    static SourcingDecision decide(PurchaseProfile profile,
                                   Map<String, List<ProviderListing>> resultsByProvider,
                                   Map<String, String> providerFailures) {
        // Step 2 — normalize and deduplicate: the same physical seat offered by two
        // providers is one candidate; keep the cheaper offer.
        var bySeat = new LinkedHashMap<String, ProviderListing>();
        resultsByProvider.values().stream().flatMap(List::stream).forEach(l ->
                bySeat.merge(l.seatKey(), l,
                        (a, b) -> a.total().compareTo(b.total()) <= 0 ? a : b));

        // Step 3 — eligibility is judged in the customer's units: the all-in total.
        var eligible = bySeat.values().stream()
                .filter(l -> l.total().compareTo(profile.maxTotalPrice()) <= 0)
                .toList();

        // Step 4 — rank against DECLARED preferences, deterministically.
        boolean sectionPreferenceMet = profile.preferredSection() != null && eligible.stream()
                .anyMatch(l -> l.section().equalsIgnoreCase(profile.preferredSection()));
        var satisfying = sectionPreferenceMet
                ? eligible.stream().filter(l -> l.section().equalsIgnoreCase(profile.preferredSection())).toList()
                : eligible;

        var ranked = satisfying.stream()
                .sorted(Comparator.comparing(ProviderListing::total)
                        .thenComparing(ProviderListing::provider)
                        .thenComparing(ProviderListing::listingId))
                .map(l -> new RankedListing(l, reasonsFor(l, profile, sectionPreferenceMet)))
                .toList();

        ProviderListing selected = ranked.isEmpty() ? null : ranked.getFirst().listing();

        // Step 5 — disclose the selection, the reasons, and every deviation.
        var reasons = new ArrayList<String>();
        String exception = null;
        if (selected != null) {
            reasons.add("LOWEST_TOTAL_COST");
            if (sectionPreferenceMet) reasons.add("MATCHES_PREFERRED_SECTION");
        }
        if (!providerFailures.isEmpty()) {
            exception = "Provider(s) failed and were not searched: " + providerFailures;
        }
        if (selected == null) {
            exception = joinExceptions(exception,
                    "No listing satisfied the profile (max all-in %s%s) across %d provider(s)"
                            .formatted(profile.maxTotalPrice(),
                                    profile.preferredSection() == null ? "" : ", preferred section " + profile.preferredSection(),
                                    resultsByProvider.size()));
        } else if (profile.preferredSection() != null && !sectionPreferenceMet) {
            exception = joinExceptions(exception,
                    "No listing in preferred section '%s' under budget; fell back to lowest total cost"
                            .formatted(profile.preferredSection()));
        }

        return new SourcingDecision(selected, ranked,
                List.copyOf(resultsByProvider.keySet()), Map.copyOf(providerFailures),
                List.copyOf(reasons), exception);
    }

    private static List<String> reasonsFor(ProviderListing l, PurchaseProfile profile, boolean sectionMet) {
        var codes = new ArrayList<String>();
        codes.add("WITHIN_BUDGET_ALL_IN");
        if (sectionMet && l.section().equalsIgnoreCase(profile.preferredSection())) {
            codes.add("MATCHES_PREFERRED_SECTION");
        }
        return List.copyOf(codes);
    }

    /// The postcondition, as code — run it after every decision. If this is false and
    /// no exception record explains why, the policy (not the market) is broken.
    public static boolean postconditionHolds(PurchaseProfile profile, SourcingDecision decision) {
        if (decision.selected() == null) return decision.exceptionRecord() != null;
        boolean isMinimum = decision.candidates().stream()
                .allMatch(c -> decision.selected().total().compareTo(c.listing().total()) <= 0);
        boolean underBudget = decision.selected().total().compareTo(profile.maxTotalPrice()) <= 0;
        return (isMinimum && underBudget) || decision.exceptionRecord() != null;
    }

    private static String joinExceptions(String a, String b) {
        return a == null ? b : a + "; " + b;
    }

    private SourcingPolicy() { }
}
