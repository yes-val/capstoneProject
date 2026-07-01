import kz.epam.campus.dao.NotificationDao;
import kz.epam.campus.model.Notification;
import kz.epam.campus.model.NotificationStatus;
import kz.epam.campus.services.EmailService;
import kz.epam.campus.services.NotificationService;

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
    private NotificationService notificationService;

    private static final int USER_ID = 1;
    private static final int BOOKING_ID = 100;
    private static final String EMAIL = "user@example.com";

    // ---------------------------------------------------------------
    // sendConfirmation / sendCancellation / sendReminder
    // ---------------------------------------------------------------

    @Test
    void sendConfirmation_success_savesNotificationAndSendsEmail() throws Exception {
        // GIVEN - default mock behavior: emailService.send() succeeds

        // WHEN
        notificationService.sendConfirmation(USER_ID, BOOKING_ID, EMAIL);

        // THEN
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationDao).save(captor.capture());
        verify(emailService).send(eq(EMAIL), anyString());
        assertEquals(NotificationStatus.CONFIRMATION, captor.getValue().getStatus());
        assertEquals(USER_ID, captor.getValue().getUserId());
        assertEquals(BOOKING_ID, captor.getValue().getBookingId());
    }

    @Test
    void sendCancellation_success_savesNotificationAndSendsEmail() throws Exception {
        // GIVEN - default mock behavior: emailService.send() succeeds

        // WHEN
        notificationService.sendCancellation(USER_ID, BOOKING_ID, EMAIL);

        // THEN
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationDao).save(captor.capture());
        verify(emailService).send(eq(EMAIL), anyString());
        assertEquals(NotificationStatus.CANCELLATION, captor.getValue().getStatus());
    }

    @Test
    void sendReminder_success_savesNotificationAndSendsEmail() throws Exception {
        // GIVEN - default mock behavior: emailService.send() succeeds

        // WHEN
        notificationService.sendReminder(USER_ID, BOOKING_ID, EMAIL);

        // THEN
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationDao).save(captor.capture());
        verify(emailService).send(eq(EMAIL), anyString());
        assertEquals(NotificationStatus.REMINDER, captor.getValue().getStatus());
    }

    @Test
    void sendConfirmation_stillSavesNotification_whenEmailSendingFails() throws Exception {
        // GIVEN
        doThrow(new MessagingException("SMTP unavailable")).when(emailService).send(eq(EMAIL), anyString());

        // WHEN
        assertDoesNotThrow(() -> notificationService.sendConfirmation(USER_ID, BOOKING_ID, EMAIL));

        // THEN
        verify(notificationDao).save(any(Notification.class));
        verify(emailService).send(eq(EMAIL), anyString());
    }

    // ---------------------------------------------------------------
    // getUserNotifications
    // ---------------------------------------------------------------

    @Test
    void getUserNotifications_returnsNotificationsFromDao() {
        // GIVEN
        Notification n1 = new Notification();
        n1.setUserId(USER_ID);
        n1.setStatus(NotificationStatus.CONFIRMATION);
        List<Notification> expected = List.of(n1);
        when(notificationDao.findByUserId(USER_ID)).thenReturn(expected);

        // WHEN
        List<Notification> result = notificationService.getUserNotifications(USER_ID);

        // THEN
        verify(notificationDao).findByUserId(USER_ID);
        assertEquals(expected, result);
    }

    @Test
    void getUserNotifications_returnsEmptyList_whenNoneExist() {
        // GIVEN
        when(notificationDao.findByUserId(USER_ID)).thenReturn(List.of());

        // WHEN
        List<Notification> result = notificationService.getUserNotifications(USER_ID);

        // THEN
        verify(notificationDao).findByUserId(USER_ID);
        assertTrue(result.isEmpty());
    }
}
