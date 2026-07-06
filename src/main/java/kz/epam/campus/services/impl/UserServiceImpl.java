package kz.epam.campus.services.impl;

import kz.epam.campus.dao.UserDao;
import kz.epam.campus.model.User;
import kz.epam.campus.services.BookingException;
import kz.epam.campus.services.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserDao userDao;
    private final PasswordEncoder encoder;

    public UserServiceImpl(UserDao userDao, PasswordEncoder encoder) {
        this.userDao = userDao;
        this.encoder = encoder;
    }

    @Transactional
    public User register(User user) {
        user.setPasswordHash(encoder.encode(user.getPasswordHash()));
        return userDao.save(user);
    }

    public Optional<User> authenticate(String email, String rawPassword) {
        return userDao.findByEmail(email)
                .filter(u -> encoder.matches(rawPassword, u.getPasswordHash()));
    }

    public boolean emailExists(String email) {
        return userDao.findByEmail(email).isPresent();
    }

    public Optional<User> findByEmail(String email) {
        return userDao.findByEmail(email);
    }

    public List<User> getAllUsers() {
        return userDao.findAll();
    }

    public List<User> getAllUsers(int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return userDao.findAll(offset, pageSize);
    }

    public int countAllUsers() {
        return userDao.countAll();
    }

    public void deactivateUser(int userId) {
        User user = userDao.findById(userId)
                .orElseThrow(() -> new BookingException("User not found"));
        user.setActive(false);
        userDao.save(user);
    }

    @Transactional
    public void updateProfile(User user, boolean passwordChanged) {
        if (passwordChanged) {
            user.setPasswordHash(encoder.encode(user.getPasswordHash()));
        }
        userDao.save(user);
    }
}