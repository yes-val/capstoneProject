package kz.epam.campus.services;

import kz.epam.campus.model.Equipment;

import java.util.List;

public interface EquipmentService {

    List<Equipment> getActiveEquipment();

    Equipment getById(int equipmentId);

    void createEquipment(Equipment equipment);

    void updateEquipment(Equipment equipment);

    void deactivateEquipment(int equipmentId);
}
