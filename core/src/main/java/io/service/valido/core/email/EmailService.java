package io.service.valido.core.email;

import jakarta.mail.MessagingException;

public interface EmailService {
    void sendEmail(String to, String subject, String text);

    void sendHtmlEmail(String to, String subject, String htmlBody) throws MessagingException;
}
