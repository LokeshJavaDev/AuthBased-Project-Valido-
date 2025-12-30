package io.service.valido.core.email.impl;

import io.service.valido.core.email.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class JavaEmailService implements EmailService {
    private static final Logger LOGGER = LoggerFactory.getLogger(JavaEmailService.class);

    private final JavaMailSender javaMailSender;

    @Value("${io.valido.email.from}")
    private String fromAddress;

    public JavaEmailService(JavaMailSender javaMailSender){
        this.javaMailSender = javaMailSender;
    }


    @Override
    public void sendEmail(String to, String subject, String text) {
        try{
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            message.setFrom(fromAddress);
            javaMailSender.send(message);
        }catch (Exception e){
            LOGGER.error("Failed to send email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send email", e);
        }
    }

    @Override
    public void sendHtmlEmail(String to, String subject, String htmlBody) throws MessagingException {
        try{
            MimeMessage mimeMessage =  javaMailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody,true);
            javaMailSender.send(mimeMessage);

        } catch (Exception e) {
            LOGGER.error("Failed to Send HTML email to {}:{}", to, e.getMessage());
        }
    }

}
