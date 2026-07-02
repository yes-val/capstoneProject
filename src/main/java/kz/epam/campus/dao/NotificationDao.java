package kz.epam.campus.dao;

import kz.epam.campus.model.Notification;

import java.util.List;

public interface NotificationDao extends CommonDao<Notification, Integer> {

    List<Notification> findByUserId(int userId);
}