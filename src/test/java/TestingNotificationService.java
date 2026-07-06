import kz.epam.campus.dao.NotificationDao;
import kz.epam.campus.model.Notification;
import kz.epam.campus.model.NotificationStatus;
import kz.epam.campus.services.EmailService;
import kz.epam.campus.services.impl.NotificationServiceImpl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.mail.MessagingException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TestingNotificationService {

    @Mock
    private NotificationDao notificationDao;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private static final int USER_ID = 1;
    private static final int BOOKING_ID = 100;
    private static final String EMAIL = "user@example.com";

    @Test
    void shouldSaveNotificationAndSendEmailOnConfirmation() throws Exception {
        notificationService.sendConfirmation(USER_ID, BOOKING_ID, EMAIL);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationDao).save(captor.capture());
        verify(emailService).send(eq(EMAIL), anyString());
        assertEquals(NotificationStatus.CONFIRMATION, captor.getValue().getStatus());
        assertEquals(USER_ID, captor.getValue().getUserId());
        assertEquals(BOOKING_ID, captor.getValue().getBookingId());
    }

    @Test
    void shouldSaveNotificationAndSendEmailOnCancellation() throws Exception {
        notificationService.sendCancellation(USER_ID, BOOKING_ID, EMAIL);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationDao).save(captor.capture());
        verify(emailService).send(eq(EMAIL), anyString());
        assertEquals(NotificationStatus.CANCELLATION, captor.getValue().getStatus());
    }

    @Test
    void shouldSaveNotificationAndSendEmailOnReminder() throws Exception {
        notificationService.sendReminder(USER_ID, BOOKING_ID, EMAIL);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationDao).save(captor.capture());
        verify(emailService).send(eq(EMAIL), anyString());
        assertEquals(NotificationStatus.REMINDER, captor.getValue().getStatus());
    }

    @Test
    void shouldStillSaveNotificationWhenEmailSendingFails() throws Exception {
        doThrow(new MessagingException("SMTP unavailable")).when(emailService).send(eq(EMAIL), anyString());

        assertDoesNotThrow(() -> notificationService.sendConfirmation(USER_ID, BOOKING_ID, EMAIL));

        verify(notificationDao).save(any(Notification.class));
        verify(emailService).send(eq(EMAIL), anyString());
    }

    @Test
    void shouldReturnNotificationsFromDao() {
        Notification n1 = new Notification();
        n1.setUserId(USER_ID);
        n1.setStatus(NotificationStatus.CONFIRMATION);
        List<Notification> expected = List.of(n1);
        when(notificationDao.findByUserId(USER_ID)).thenReturn(expected);

        List<Notification> result = notificationService.getUserNotifications(USER_ID);

        verify(notificationDao).findByUserId(USER_ID);
        assertEquals(expected, result);
    }

    @Test
    void shouldReturnEmptyListWhenNoneExist() {
        when(notificationDao.findByUserId(USER_ID)).thenReturn(List.of());

        List<Notification> result = notificationService.getUserNotifications(USER_ID);

        verify(notificationDao).findByUserId(USER_ID);
        assertTrue(result.isEmpty());
    }
}
