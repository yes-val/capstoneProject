package kz.epam.campus.dao;

import kz.epam.campus.model.User;

import java.util.List;
import java.util.Optional;

public interface UserDao extends CommonDao<User, Integer> {

    Optional<User> findByEmail(String email);

    List<User> findAll(int offset, int limit);

    int countAll();
}

