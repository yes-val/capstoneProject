package kz.epam.campus.services;

import kz.epam.campus.model.Notification;

import java.util.List;

public interface NotificationService {

    void sendConfirmation(int userId, int bookingId, String email);

    void sendCancellation(int userId, int bookingId, String email);

    void sendReminder(int userId, int bookingId, String email);

    List<Notification> getUserNotifications(int userId);
}
