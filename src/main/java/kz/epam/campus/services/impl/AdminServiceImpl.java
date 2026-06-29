package kz.epam.campus.services.impl;

import kz.epam.campus.dao.EquipmentDao;
import kz.epam.campus.dao.UserDao;
import kz.epam.campus.model.Equipment;
import kz.epam.campus.model.User;
import kz.epam.campus.services.AdminService;
import org.springframework.security.crypto.password.PasswordEncoder;

public class AdminServiceImpl implements AdminService {

    private final UserDao userDao;
    private final EquipmentDao equipmentDao;
    private final PasswordEncoder encoder;

    public AdminServiceImpl(UserDao userDao,
                            EquipmentDao equipmentDao,
                            PasswordEncoder encoder) {
        this.userDao = userDao;
        this.equipmentDao = equipmentDao;
        this.encoder = encoder;
    }

    public User createUser(User user) {
        user.setPasswordHash(encoder.encode(user.getPasswordHash()));
        return userDao.save(user);
    }

    public void deactivateUser(int userId) {
        userDao.findById(userId).ifPresent(u -> {
            u.setActive(false);
            userDao.save(u);
        });
    }

    public Equipment addEquipment(Equipment equipment) {
        return equipmentDao.save(equipment);
    }

    public void deactivateEquipment(int equipmentId) {
        equipmentDao.findById(equipmentId).ifPresent(e -> {
            e.setActive(false);
            equipmentDao.save(e);
        });
    }
}