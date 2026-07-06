package kz.epam.campus.services.security;

import kz.epam.campus.model.User;

import java.util.Optional;

public interface CurrentUserService {
    Optional<String> getCurrentUsername();
}
