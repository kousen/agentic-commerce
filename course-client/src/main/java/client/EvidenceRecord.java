package client;

import java.util.List;
import java.util.Map;

import client.SourcingPolicy.SourcingDecision;

/// Module 3, §3.3 — the record that answers "why did you buy that?"
/// Checkable fields, not narrative. If you cannot produce this, someone will
/// eventually produce a chargeback instead.
public record EvidenceRecord(
        List<String> providersSearched,
        Map<String, String> providerFailures,
        int candidatesConsidered,
        String rankingPolicy,
        String profileVersion,
        String selectedListing,     // null if nothing was selected
        List<String> selectionReasons,
        String exceptionRecord,     // null when the simple rule applied cleanly
        boolean postconditionHeld) {

    public static EvidenceRecord from(PurchaseProfile profile, SourcingDecision decision) {
        var selected = decision.selected();
        return new EvidenceRecord(
                decision.providersSearched(),
                decision.providerFailures(),
                decision.candidates().size(),
                SourcingPolicy.POLICY_ID,
                profile.version(),
                selected == null ? null
                        : "%s #%d — %s %s row %s seat %s at $%s all-in".formatted(
                                selected.provider(), selected.listingId(), selected.event(),
                                selected.section(), selected.row(), selected.seat(), selected.total()),
                decision.selectionReasons(),
                decision.exceptionRecord(),
                SourcingPolicy.postconditionHolds(profile, decision));
    }

    public String render() {
        return """
                ── purchase evidence ─────────────────────────────────────────
                providers searched .... %s
                provider failures ..... %s
                candidates considered . %d
                ranking policy ........ %s
                profile version ....... %s
                selected .............. %s
                selection reasons ..... %s
                exception record ...... %s
                postcondition held .... %s
                ──────────────────────────────────────────────────────────────"""
                .formatted(providersSearched, providerFailures.isEmpty() ? "none" : providerFailures,
                        candidatesConsidered, rankingPolicy, profileVersion,
                        selectedListing == null ? "NOTHING — see exception record" : selectedListing,
                        selectionReasons, exceptionRecord == null ? "none" : exceptionRecord,
                        postconditionHeld);
    }
}
