package kz.epam.campus.services.security.impl;

import kz.epam.campus.services.security.PasswordEncoderService;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordEncoderServiceImpl implements PasswordEncoderService {

    private final PasswordEncoder encoder;

    public PasswordEncoderServiceImpl(PasswordEncoder encoder) {
        this.encoder = encoder;
    }

    public String encode(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    public boolean matches(String rawPassword, String encodedPassword) {
        return encoder.matches(rawPassword, encodedPassword);
    }
}

//TODO: @PasswordEncoder should match its Bean name