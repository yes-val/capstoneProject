package kz.epam.campus.services;

import kz.epam.campus.model.Slot;

import java.time.LocalDate;
import java.util.List;

public interface SlotService {

    void generateSlotsForDate(LocalDate date);

    List<Slot> getSlots(int equipmentId, LocalDate date);

    List<Slot> getAvailableSlots(int equipmentId, LocalDate date);

    boolean slotsExist(int equipmentId, LocalDate date);

    Slot getSlotById(int slotId);
}
