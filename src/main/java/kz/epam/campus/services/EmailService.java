package kz.epam.campus.services;

import javax.mail.MessagingException;

public interface EmailService {

    void send(String toAddress, String message) throws MessagingException;
}
