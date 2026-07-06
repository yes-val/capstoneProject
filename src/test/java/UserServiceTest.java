import kz.epam.campus.model.User;
import kz.epam.campus.model.Role;
import kz.epam.campus.services.UserService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest extends BaseDbTest {

    private final UserService userService =
            ctx.getBean(UserService.class);

    @Test
    void shouldRegisterUser() {
        User u = new User();
        u.setName("John");
        u.setEmail("john@test.com");
        u.setPasswordHash("123");
        u.setPosition(Role.USER);
        u.setActive(true);

        User saved = userService.register(u);

        assertTrue(saved.getUserId() > 0);
    }

    @Test
    void shouldAuthenticate() {
        userService.register(createUser());

        assertTrue(
                userService.authenticate("john@test.com", "123").isPresent()
        );
    }

    @Test
    void shouldFailAuthenticationWithWrongPassword() {
        userService.register(createUser());

        assertTrue(
                userService.authenticate("john@test.com", "wrong").isEmpty()
        );
    }

    private User createUser() {
        User u = new User();
        u.setName("John");
        u.setEmail("john@test.com");
        u.setPasswordHash("123");
        u.setPosition(Role.USER);
        u.setActive(true);
        return u;
    }
}