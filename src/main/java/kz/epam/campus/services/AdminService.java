package kz.epam.campus.services;

import kz.epam.campus.model.User;
import kz.epam.campus.model.Equipment;

public interface AdminService {
    User createUser(User user);

    void deactivateUser(int userId);

    Equipment addEquipment(Equipment equipment);

    void deactivateEquipment(int equipmentId);
}