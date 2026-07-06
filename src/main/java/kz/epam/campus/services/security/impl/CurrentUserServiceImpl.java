package kz.epam.campus.services.security.impl;


import kz.epam.campus.services.security.CurrentUserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public class CurrentUserServiceImpl implements CurrentUserService {

    public Optional<String> getCurrentUsername() {
        Authentication auth = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            return Optional.empty();
        }

        return Optional.ofNullable(auth.getName());
    }
}