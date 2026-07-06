import kz.epam.campus.dao.UserDao;
import kz.epam.campus.model.User;
import kz.epam.campus.services.BookingException;
import kz.epam.campus.services.impl.UserServiceImpl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TestingUserService {

    @Mock
    private UserDao userDao;

    @Mock
    private PasswordEncoder encoder;

    @InjectMocks
    private UserServiceImpl userService;

    private static final int USER_ID = 1;
    private static final String EMAIL = "user@example.com";

    private User user(int userId, String email, String passwordHash) {
        User user = new User();
        user.setUserId(userId);
        user.setEmail(email);
        user.setPasswordHash(passwordHash);
        user.setActive(true);
        return user;
    }

    @Test
    void shouldEncodePasswordAndSaveUser() {
        User newUser = user(0, EMAIL, "rawPassword");
        when(encoder.encode("rawPassword")).thenReturn("encodedPassword");
        when(userDao.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.register(newUser);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userDao).save(captor.capture());
        assertEquals("encodedPassword", captor.getValue().getPasswordHash());
        assertEquals("encodedPassword", result.getPasswordHash());
    }

    @Test
    void shouldAuthenticateWhenEmailFoundAndPasswordMatches() {
        User existing = user(USER_ID, EMAIL, "encodedPassword");
        when(userDao.findByEmail(EMAIL)).thenReturn(Optional.of(existing));
        when(encoder.matches("rawPassword", "encodedPassword")).thenReturn(true);

        Optional<User> result = userService.authenticate(EMAIL, "rawPassword");

        verify(userDao).findByEmail(EMAIL);
        assertTrue(result.isPresent());
        assertEquals(USER_ID, result.get().getUserId());
    }

    @Test
    void shouldReturnEmptyWhenPasswordDoesNotMatch() {
        User existing = user(USER_ID, EMAIL, "encodedPassword");
        when(userDao.findByEmail(EMAIL)).thenReturn(Optional.of(existing));
        when(encoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        Optional<User> result = userService.authenticate(EMAIL, "wrongPassword");

        verify(encoder).matches("wrongPassword", "encodedPassword");
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnEmptyWhenEmailNotFound() {
        when(userDao.findByEmail(EMAIL)).thenReturn(Optional.empty());

        Optional<User> result = userService.authenticate(EMAIL, "rawPassword");

        verify(encoder, never()).matches(anyString(), anyString());
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnTrueWhenUserFound() {
        when(userDao.findByEmail(EMAIL)).thenReturn(Optional.of(user(USER_ID, EMAIL, "hash")));

        boolean result = userService.emailExists(EMAIL);

        verify(userDao).findByEmail(EMAIL);
        assertTrue(result);
    }

    @Test
    void shouldReturnFalseWhenUserNotFound() {
        when(userDao.findByEmail(EMAIL)).thenReturn(Optional.empty());

        boolean result = userService.emailExists(EMAIL);

        verify(userDao).findByEmail(EMAIL);
        assertFalse(result);
    }

    @Test
    void shouldReturnUserWhenPresent() {
        User existing = user(USER_ID, EMAIL, "hash");
        when(userDao.findByEmail(EMAIL)).thenReturn(Optional.of(existing));

        Optional<User> result = userService.findByEmail(EMAIL);

        verify(userDao).findByEmail(EMAIL);
        assertTrue(result.isPresent());
        assertEquals(existing, result.get());
    }

    @Test
    void shouldReturnEmptyWhenAbsent() {
        when(userDao.findByEmail(EMAIL)).thenReturn(Optional.empty());

        Optional<User> result = userService.findByEmail(EMAIL);

        verify(userDao).findByEmail(EMAIL);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnAllUsersFromDao() {
        List<User> expected = List.of(user(1, "a@example.com", "h1"), user(2, "b@example.com", "h2"));
        when(userDao.findAll()).thenReturn(expected);

        List<User> result = userService.getAllUsers();

        verify(userDao).findAll();
        assertEquals(expected, result);
    }

    @Test
    void shouldReturnEmptyListWhenNoUsersExist() {
        when(userDao.findAll()).thenReturn(List.of());

        List<User> result = userService.getAllUsers();

        verify(userDao).findAll();
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldDeactivateUserWhenUserFound() {
        User existing = user(USER_ID, EMAIL, "hash");
        when(userDao.findById(USER_ID)).thenReturn(Optional.of(existing));

        userService.deactivateUser(USER_ID);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userDao).save(captor.capture());
        assertFalse(captor.getValue().isActive());
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        when(userDao.findById(USER_ID)).thenReturn(Optional.empty());

        BookingException exception = assertThrows(BookingException.class,
                () -> userService.deactivateUser(USER_ID));

        verify(userDao, never()).save(any());
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void shouldEncodePasswordWhenPasswordChanged() {
        User existing = user(USER_ID, EMAIL, "newRawPassword");
        when(encoder.encode("newRawPassword")).thenReturn("newEncodedPassword");

        userService.updateProfile(existing, true);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userDao).save(captor.capture());
        assertEquals("newEncodedPassword", captor.getValue().getPasswordHash());
    }

    @Test
    void shouldNotEncodePasswordWhenPasswordNotChanged() {
        User existing = user(USER_ID, EMAIL, "unchangedHash");

        userService.updateProfile(existing, false);

        verify(encoder, never()).encode(anyString());
        verify(userDao).save(existing);
        assertEquals("unchangedHash", existing.getPasswordHash());
    }
}
