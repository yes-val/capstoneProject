package kz.epam.campus.dao;

import kz.epam.campus.model.User;

import java.util.Optional;

public interface UserDao extends CommonDao<User, Integer> {

    Optional<User> findByEmail(String email);
}

