import kz.epam.campus.services.NotificationService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class NotificationTest extends BaseDbTest {

    private final NotificationService notificationService =
            ctx.getBean(NotificationService.class);

    @Test
    void sendConfirmation_success() {
        assertDoesNotThrow(() ->
                notificationService.sendConfirmation(1, 1, "test@example.com")
        );
    }

    @Test
    void sendCancellation_success() {
        assertDoesNotThrow(() ->
                notificationService.sendCancellation(1, 1, "test@example.com")
        );
    }
}