package kz.epam.campus.config;

import kz.epam.campus.services.EmailService;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class EmailConfigLoader {

    public static EmailService load() {

        Properties props = new Properties();

        try (InputStream in = EmailConfigLoader.class
                .getClassLoader()
                .getResourceAsStream("mail.properties")) {

            if (in == null) {
                throw new IllegalStateException("mail.properties not found on classpath");
            }

            props.load(in);

        } catch (IOException e) {
            throw new IllegalStateException("Failed to load mail.properties", e);
        }

        String host = props.getProperty("mail.smtp.host");
        int port = Integer.parseInt(props.getProperty("mail.smtp.port"));
        String username = props.getProperty("mail.smtp.username");
        String password = props.getProperty("mail.smtp.password");
        String from = props.getProperty("mail.smtp.from");

        return new EmailService(host, port, username, password, from);
    }
}