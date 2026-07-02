package kz.epam.campus.dao;

import kz.epam.campus.model.Equipment;

import java.util.List;

public interface EquipmentDao extends CommonDao<Equipment, Integer> {

    List<Equipment> findAllActive();
}