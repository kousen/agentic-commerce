package client;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

/// The course's buyer-side reference client.
///
/// Default run — deterministic sourcing against hosted MockHub, no AI involved:
///     ./gradlew bootRun
/// Optionally override the ask:
///     ./gradlew bootRun --args="'Monster Jam' 45.00 'Lower Level'"
///
/// Agent run — the same policy exposed as tools to a Spring AI agent (see agent/):
///     ANTHROPIC_API_KEY=... SPRING_PROFILES_ACTIVE=agent ./gradlew bootRun
@SpringBootApplication
public class CourseClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(CourseClientApplication.class, args);
    }

    @Bean
    @Profile("!agent")
    CommandLineRunner sourceAndDisclose(
            @Value("${mockhub.base-url}") String baseUrl,
            @Value("${mockhub.acp-key}") String acpKey) {
        return args -> {
            var profile = PurchaseProfile.of(
                    args.length > 0 ? args[0] : "Hamilton",
                    args.length > 1 ? args[1] : "60.00",
                    args.length > 2 ? args[2] : "Orchestra");

            // Two providers, identical inventory — the §1.2 demo pair. The policy
            // must search both, deduplicate the shared seats, and disclose all of it.
            var providers = List.<Providers.TicketProvider>of(
                    new MockHubProvider("TicketNexus", baseUrl, acpKey),
                    new MockHubProvider("SeatStream", baseUrl, acpKey));

            var decision = SourcingPolicy.source(profile, providers);
            var evidence = EvidenceRecord.from(profile, decision);

            System.out.printf("profile: %s under $%s all-in%s%n%n",
                    profile.eventQuery(), profile.maxTotalPrice(),
                    profile.preferredSection() == null ? "" : ", preferring " + profile.preferredSection());
            System.out.println(evidence.render());
        };
    }
}
