package kz.epam.campus.services;

import kz.epam.campus.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    User register(User user);

    Optional<User> authenticate(String email, String rawPassword);

    boolean emailExists(String email);

    Optional<User> findByEmail(String email);

    List<User> getAllUsers();

    void deactivateUser(int userId);
    void updateProfile(User user, boolean passwordChanged);
}