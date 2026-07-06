package kz.epam.campus.services.security.impl;

import kz.epam.campus.services.security.SecurityService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class SecurityServiceImpl implements SecurityService {

    public boolean isAdmin() {
        return hasRole("ROLE_ADMIN");
    }

    public boolean isUser() {
        return hasRole("ROLE_USER");
    }

    private boolean hasRole(String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null) return false;

        return auth.getAuthorities()
                .contains(new SimpleGrantedAuthority(role));
    }
}
