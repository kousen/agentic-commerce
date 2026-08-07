package client.agent;

import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import client.EvidenceRecord;
import client.MockHubProvider;
import client.Providers;
import client.PurchaseProfile;
import client.SourcingPolicy;

/// The take-home Spring AI wiring: the SAME deterministic policy, exposed to a model
/// as a tool. The model converses; the policy decides. Structure crosses this
/// boundary — authority never does (the tool has no purchase capability at all).
///
/// Run:  ANTHROPIC_API_KEY=... SPRING_PROFILES_ACTIVE=agent ./gradlew bootRun
@Configuration
@Profile("agent")
public class BuyingAgent {

    private final String baseUrl;
    private final String acpKey;

    public BuyingAgent(@Value("${mockhub.base-url}") String baseUrl,
                       @Value("${mockhub.acp-key}") String acpKey) {
        this.baseUrl = baseUrl;
        this.acpKey = acpKey;
    }

    @Tool(description = "Source tickets across every connected provider using the customer's "
            + "declared budget (all-in) and section preference. Returns the full evidence "
            + "record: providers searched, candidates, selection, reasons, and exceptions. "
            + "This tool searches and ranks; it cannot purchase.")
    public String sourceTickets(
            @ToolParam(description = "Event to search for, e.g. 'Hamilton'") String eventQuery,
            @ToolParam(description = "Maximum ALL-IN price the customer will pay, e.g. '60.00'") String maxTotalPrice,
            @ToolParam(description = "Preferred section, or empty for no preference") String preferredSection) {
        var profile = PurchaseProfile.of(eventQuery, maxTotalPrice,
                preferredSection == null || preferredSection.isBlank() ? null : preferredSection);
        var providers = List.<Providers.TicketProvider>of(
                new MockHubProvider("TicketNexus", baseUrl, acpKey),
                new MockHubProvider("SeatStream", baseUrl, acpKey));
        var decision = SourcingPolicy.source(profile, providers);
        return EvidenceRecord.from(profile, decision).render();
    }

    @Bean
    CommandLineRunner agentConversation(ChatClient.Builder builder) {
        return args -> {
            var chat = builder
                    .defaultSystem("""
                            You are a careful ticket-buying assistant. Use the sourceTickets tool
                            for every availability question — never estimate prices yourself.
                            Relay the evidence record's disclosures to the customer, including any
                            provider failures or exception records, verbatim where they matter.""")
                    .defaultTools(this)
                    .build();

            String question = args.length > 0 ? String.join(" ", args)
                    : "Find me one Hamilton ticket under $60 all-in, Orchestra if possible. "
                    + "Tell me what you searched and why you picked what you picked.";

            System.out.println("[customer] " + question + "\n");
            System.out.println("[agent] " + chat.prompt().user(question).call().content());
        };
    }
}
