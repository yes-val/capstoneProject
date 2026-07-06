package kz.epam.campus.services.impl;

import kz.epam.campus.dao.EquipmentDao;
import kz.epam.campus.dao.SlotDao;
import kz.epam.campus.model.Equipment;
import kz.epam.campus.model.Slot;
import kz.epam.campus.services.BookingException;
import kz.epam.campus.services.LabHours;
import kz.epam.campus.services.ScheduleService;
import kz.epam.campus.services.SlotService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class SlotServiceImpl implements SlotService {

    private final SlotDao slotDao;
    private final EquipmentDao equipmentDao;
    private final ScheduleService scheduleService;

    public SlotServiceImpl(SlotDao slotDao, EquipmentDao equipmentDao, ScheduleService scheduleService) {
        this.slotDao = slotDao;
        this.equipmentDao = equipmentDao;
        this.scheduleService = scheduleService;
    }

    @Override
    public void generateSlotsForDate(LocalDate date) {

        if (!scheduleService.isWorkingDay(date)) {
            return;
        }

        List<Equipment> activeEquipment = equipmentDao.findAllActive();

        for (Equipment equipment : activeEquipment) {

            if (slotDao.existsByEquipmentIdAndDate(equipment.getEquipmentId(), date)) {
                continue;
            }

            LocalTime current = LabHours.DEFAULT_START;
            while (current.isBefore(LabHours.DEFAULT_END)) {
                Slot slot = new Slot();
                slot.setEquipmentId(equipment.getEquipmentId());
                slot.setDate(date);
                slot.setTimeStart(current);
                slotDao.save(slot);
                current = current.plusHours(1);
            }
        }
    }

    @Override
    public List<Slot> getSlots(int equipmentId, LocalDate date) {
        return slotDao.findByEquipmentIdAndDate(equipmentId, date);
    }

    @Override
    public List<Slot> getAvailableSlots(int equipmentId, LocalDate date) {
        return slotDao.findUnbookedByEquipmentIdAndDate(equipmentId, date);
    }

    @Override
    public boolean slotsExist(int equipmentId, LocalDate date) {
        return slotDao.existsByEquipmentIdAndDate(equipmentId, date);
    }

    @Override
    public Slot getSlotById(int slotId) {
        return slotDao.findById(slotId).orElseThrow(() -> new BookingException("Slot not found"));
    }
}
