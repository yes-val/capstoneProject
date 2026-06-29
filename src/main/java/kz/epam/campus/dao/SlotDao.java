package kz.epam.campus.dao;

import kz.epam.campus.model.Slot;

import java.time.LocalDate;
import java.util.List;

public interface SlotDao extends DaoInterface<Slot, Integer> {
    List<Slot> findByEquipmentIdAndDate(int equipmentId, LocalDate date);

    List<Slot> findUnbookedByEquipmentIdAndDate(int equipmentId, LocalDate date);

    boolean existsByEquipmentIdAndDate(int equipmentId, LocalDate date);
}