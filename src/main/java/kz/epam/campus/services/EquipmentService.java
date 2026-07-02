package kz.epam.campus.services;
// discrete IF
import kz.epam.campus.dao.EquipmentDao;
import kz.epam.campus.model.Equipment;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class EquipmentService {

    private final EquipmentDao equipmentDao;

    public EquipmentService(EquipmentDao equipmentDao) {
        this.equipmentDao = equipmentDao;
    }

    public List<Equipment> getActiveEquipment() {
        return equipmentDao.findAllActive();
    }

    public Equipment getById(int equipmentId) {
        return equipmentDao.findById(equipmentId)
                .orElseThrow(() -> new BookingException("Equipment not found"));
    }

    public void createEquipment(Equipment equipment) {
        equipment.setActive(true);
        equipmentDao.save(equipment);
    }

    public void updateEquipment(Equipment equipment) {
        equipmentDao.save(equipment);
    }

    public void deactivateEquipment(int equipmentId) {
        Equipment equipment = getById(equipmentId);
        equipment.setActive(false);
        equipmentDao.save(equipment);
    }
}
