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

    // ---------------------------------------------------------------
    // register
    // ---------------------------------------------------------------

    @Test
    void register_success_encodesPasswordAndSavesUser() {
        // GIVEN
        User newUser = user(0, EMAIL, "rawPassword");
        when(encoder.encode("rawPassword")).thenReturn("encodedPassword");
        when(userDao.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // WHEN
        User result = userService.register(newUser);

        // THEN
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userDao).save(captor.capture());
        assertEquals("encodedPassword", captor.getValue().getPasswordHash());
        assertEquals("encodedPassword", result.getPasswordHash());
    }

    // ---------------------------------------------------------------
    // authenticate
    // ---------------------------------------------------------------

    @Test
    void authenticate_success_whenEmailFoundAndPasswordMatches() {
        // GIVEN
        User existing = user(USER_ID, EMAIL, "encodedPassword");
        when(userDao.findByEmail(EMAIL)).thenReturn(Optional.of(existing));
        when(encoder.matches("rawPassword", "encodedPassword")).thenReturn(true);

        // WHEN
        Optional<User> result = userService.authenticate(EMAIL, "rawPassword");

        // THEN
        verify(userDao).findByEmail(EMAIL);
        assertTrue(result.isPresent());
        assertEquals(USER_ID, result.get().getUserId());
    }

    @Test
    void authenticate_returnsEmpty_whenPasswordDoesNotMatch() {
        // GIVEN
        User existing = user(USER_ID, EMAIL, "encodedPassword");
        when(userDao.findByEmail(EMAIL)).thenReturn(Optional.of(existing));
        when(encoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        // WHEN
        Optional<User> result = userService.authenticate(EMAIL, "wrongPassword");

        // THEN
        verify(encoder).matches("wrongPassword", "encodedPassword");
        assertTrue(result.isEmpty());
    }

    @Test
    void authenticate_returnsEmpty_whenEmailNotFound() {
        // GIVEN
        when(userDao.findByEmail(EMAIL)).thenReturn(Optional.empty());

        // WHEN
        Optional<User> result = userService.authenticate(EMAIL, "rawPassword");

        // THEN
        verify(encoder, never()).matches(anyString(), anyString());
        assertTrue(result.isEmpty());
    }

    // ---------------------------------------------------------------
    // emailExists
    // ---------------------------------------------------------------

    @Test
    void emailExists_returnsTrue_whenUserFound() {
        // GIVEN
        when(userDao.findByEmail(EMAIL)).thenReturn(Optional.of(user(USER_ID, EMAIL, "hash")));

        // WHEN
        boolean result = userService.emailExists(EMAIL);

        // THEN
        verify(userDao).findByEmail(EMAIL);
        assertTrue(result);
    }

    @Test
    void emailExists_returnsFalse_whenUserNotFound() {
        // GIVEN
        when(userDao.findByEmail(EMAIL)).thenReturn(Optional.empty());

        // WHEN
        boolean result = userService.emailExists(EMAIL);

        // THEN
        verify(userDao).findByEmail(EMAIL);
        assertFalse(result);
    }

    // ---------------------------------------------------------------
    // findByEmail
    // ---------------------------------------------------------------

    @Test
    void findByEmail_returnsUser_whenPresent() {
        // GIVEN
        User existing = user(USER_ID, EMAIL, "hash");
        when(userDao.findByEmail(EMAIL)).thenReturn(Optional.of(existing));

        // WHEN
        Optional<User> result = userService.findByEmail(EMAIL);

        // THEN
        verify(userDao).findByEmail(EMAIL);
        assertTrue(result.isPresent());
        assertEquals(existing, result.get());
    }

    @Test
    void findByEmail_returnsEmpty_whenAbsent() {
        // GIVEN
        when(userDao.findByEmail(EMAIL)).thenReturn(Optional.empty());

        // WHEN
        Optional<User> result = userService.findByEmail(EMAIL);

        // THEN
        verify(userDao).findByEmail(EMAIL);
        assertTrue(result.isEmpty());
    }

    // ---------------------------------------------------------------
    // getAllUsers
    // ---------------------------------------------------------------

    @Test
    void getAllUsers_returnsAllUsersFromDao() {
        // GIVEN
        List<User> expected = List.of(user(1, "a@example.com", "h1"), user(2, "b@example.com", "h2"));
        when(userDao.findAll()).thenReturn(expected);

        // WHEN
        List<User> result = userService.getAllUsers();

        // THEN
        verify(userDao).findAll();
        assertEquals(expected, result);
    }

    @Test
    void getAllUsers_returnsEmptyList_whenNoUsersExist() {
        // GIVEN
        when(userDao.findAll()).thenReturn(List.of());

        // WHEN
        List<User> result = userService.getAllUsers();

        // THEN
        verify(userDao).findAll();
        assertTrue(result.isEmpty());
    }

    // ---------------------------------------------------------------
    // deactivateUser
    // ---------------------------------------------------------------

    @Test
    void deactivateUser_success_whenUserFound() {
        // GIVEN
        User existing = user(USER_ID, EMAIL, "hash");
        when(userDao.findById(USER_ID)).thenReturn(Optional.of(existing));

        // WHEN
        userService.deactivateUser(USER_ID);

        // THEN
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userDao).save(captor.capture());
        assertFalse(captor.getValue().isActive());
    }

    @Test
    void deactivateUser_throwsException_whenUserNotFound() {
        // GIVEN
        when(userDao.findById(USER_ID)).thenReturn(Optional.empty());

        // WHEN
        BookingException exception = assertThrows(BookingException.class,
                () -> userService.deactivateUser(USER_ID));

        // THEN
        verify(userDao, never()).save(any());
        assertEquals("User not found", exception.getMessage());
    }

    // ---------------------------------------------------------------
    // updateProfile
    // ---------------------------------------------------------------

    @Test
    void updateProfile_encodesPassword_whenPasswordChanged() {
        // GIVEN
        User existing = user(USER_ID, EMAIL, "newRawPassword");
        when(encoder.encode("newRawPassword")).thenReturn("newEncodedPassword");

        // WHEN
        userService.updateProfile(existing, true);

        // THEN
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userDao).save(captor.capture());
        assertEquals("newEncodedPassword", captor.getValue().getPasswordHash());
    }

    @Test
    void updateProfile_doesNotEncodePassword_whenPasswordNotChanged() {
        // GIVEN
        User existing = user(USER_ID, EMAIL, "unchangedHash");

        // WHEN
        userService.updateProfile(existing, false);

        // THEN
        verify(encoder, never()).encode(anyString());
        verify(userDao).save(existing);
        assertEquals("unchangedHash", existing.getPasswordHash());
    }
}
