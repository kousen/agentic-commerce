package lab;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The setup checkpoint, run at 00:15: ./gradlew checkpoint
 * Paste the CHECKPOINT line it prints into the class chat.
 */
@Tag("checkpoint")
class CheckpointTest {

    @Test
    void mockhub_is_reachable_and_healthy() {
        var response = Lab.get("/actuator/health");

        assertThat(response.statusCode())
                .as("MockHub health endpoint should answer")
                .isEqualTo(200);
        assertThat(response.body())
                .as("MockHub should report itself UP")
                .contains("\"status\":\"UP\"");

        System.out.println("CHECKPOINT OK — java — " + Lab.BASE_URL);
    }
}
