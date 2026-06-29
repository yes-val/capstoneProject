package kz.epam.campus.services.security;

public interface PasswordEncoderService {

    String encode(String rawPassword);

    boolean matches(String rawPassword, String encodedPassword);
}