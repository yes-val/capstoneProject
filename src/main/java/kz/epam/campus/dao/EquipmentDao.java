package kz.epam.campus.dao;

import kz.epam.campus.model.Equipment;

import java.util.List;

public interface EquipmentDao extends DaoInterface<Equipment, Integer> {

    List<Equipment> findAllActive();
}