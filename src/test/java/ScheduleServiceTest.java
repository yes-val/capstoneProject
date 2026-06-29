import kz.epam.campus.services.SlotService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class ScheduleServiceTest extends BaseDbTest {

    private final SlotService slotService =
            ctx.getBean(SlotService.class);

    @Test
    void generateSlots_doesNotCrash() {
        assertDoesNotThrow(() ->
                slotService.generateSlotsForDate(LocalDate.now())
        );
    }
}