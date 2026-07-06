package kz.epam.campus.services.impl;

import kz.epam.campus.dao.EquipmentDao;
import kz.epam.campus.model.Equipment;
import kz.epam.campus.services.BookingException;
import kz.epam.campus.services.EquipmentService;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class EquipmentServiceImpl implements EquipmentService {

    private final EquipmentDao equipmentDao;

    public EquipmentServiceImpl(EquipmentDao equipmentDao) {
        this.equipmentDao = equipmentDao;
    }

    @Override
    public List<Equipment> getActiveEquipment() {
        return equipmentDao.findAllActive();
    }

    @Override
    public Equipment getById(int equipmentId) {
        return equipmentDao.findById(equipmentId)
                .orElseThrow(() -> new BookingException("Equipment not found"));
    }

    @Override
    public void createEquipment(Equipment equipment) {
        equipment.setActive(true);
        equipmentDao.save(equipment);
    }

    @Override
    public void updateEquipment(Equipment equipment) {
        equipmentDao.save(equipment);
    }

    @Override
    public void deactivateEquipment(int equipmentId) {
        Equipment equipment = getById(equipmentId);
        equipment.setActive(false);
        equipmentDao.save(equipment);
    }
}
