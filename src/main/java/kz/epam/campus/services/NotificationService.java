package kz.epam.campus.services;

import kz.epam.campus.dao.NotificationDao;
import kz.epam.campus.model.Notification;
import kz.epam.campus.model.NotificationStatus;

import javax.mail.MessagingException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Logger LOGGER = Logger.getLogger(NotificationService.class.getName());

    private final NotificationDao notificationDao;
    private final EmailService emailService;

    public NotificationService(NotificationDao notificationDao, EmailService emailService) {
        this.notificationDao = notificationDao;
        this.emailService = emailService;
    }

    public void sendConfirmation(int userId, int bookingId, String email) {
        create(userId, bookingId, NotificationStatus.CONFIRMATION, email,
                "Your lab booking is confirmed.");
    }

    public void sendCancellation(int userId, int bookingId, String email) {
        create(userId, bookingId, NotificationStatus.CANCELLATION, email,
                "Your lab booking has been cancelled.");
    }

    public void sendReminder(int userId, int bookingId, String email) {
        create(userId, bookingId, NotificationStatus.REMINDER, email,
                "Reminder: you have an upcoming lab booking.");
    }

    public List<Notification> getUserNotifications(int userId) {
        return notificationDao.findByUserId(userId);
    }

    private void create(int userId, int bookingId, NotificationStatus status, String email, String message) {

        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setBookingId(bookingId);
        notification.setStatus(status);
        notification.setTimeSent(LocalDateTime.now());

        notificationDao.save(notification);

        try {
            emailService.send(email, message);
        } catch (MessagingException e) {
            LOGGER.log(Level.WARNING, "Failed to send notification email to " + email, e);
        }
    }
}
