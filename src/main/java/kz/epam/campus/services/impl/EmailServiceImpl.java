package kz.epam.campus.services.impl;

import kz.epam.campus.services.EmailService;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailServiceImpl implements EmailService {

    private final String smtpHost;
    private final int smtpPort;
    private final String username;
    private final String password;
    private final String fromAddress;

    public EmailServiceImpl(String smtpHost, int smtpPort, String username, String password, String fromAddress) {
        this.smtpHost = smtpHost;
        this.smtpPort = smtpPort;
        this.username = username;
        this.password = password;
        this.fromAddress = fromAddress;
    }

    @Override
    public void send(String toAddress, String message) throws MessagingException {

        Properties props = new Properties();
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", String.valueOf(smtpPort));
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        MimeMessage mimeMessage = new MimeMessage(session);
        mimeMessage.setFrom(new InternetAddress(fromAddress));
        mimeMessage.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toAddress));
        mimeMessage.setSubject("Engineering Lab Booking Notification");
        mimeMessage.setText(message);

        Transport.send(mimeMessage);
    }
}
