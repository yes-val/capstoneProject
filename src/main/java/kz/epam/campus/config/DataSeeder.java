package kz.epam.campus.config;

import kz.epam.campus.model.Equipment;
import kz.epam.campus.model.Role;
import kz.epam.campus.model.User;
import kz.epam.campus.services.EquipmentService;
import kz.epam.campus.services.SlotService;
import kz.epam.campus.services.UserService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.time.LocalDate;

@Profile("!test")
@Component
public class DataSeeder {

    private final EquipmentService equipmentService;
    private final SlotService slotService;
    private final UserService userService;

    public DataSeeder(EquipmentService equipmentService, SlotService slotService, UserService userService) {
        this.equipmentService = equipmentService;
        this.slotService = slotService;
        this.userService = userService;
    }

    @PostConstruct
    public void seed() {
        if (!equipmentService.getActiveEquipment().isEmpty()) {
            return;
        }

        Equipment equipment = new Equipment();
        equipment.setName("3D Printer (Test)");
        equipment.setDescription("Seeded automatically for local testing — safe to remove later.");
        equipment.setActive(true);
        equipmentService.createEquipment(equipment);

        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(13);
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            slotService.generateSlotsForDate(date);
        }

        if (userService.emailExists("admin@lab.local")) {
            return;
        }

        User admin = new User();
        admin.setName("Admin");
        admin.setEmail("admin@lab.local");
        admin.setPasswordHash("admin123");
        admin.setPosition(Role.ADMIN);
        admin.setActive(true);
        userService.register(admin);
    }
}
