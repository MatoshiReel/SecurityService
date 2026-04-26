package ru.reel.SecurityService.infrastructure.mail;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import ru.reel.SecurityService.port.mail.MailSender;

@Component
public class SimpleMailSender implements MailSender {
    private final JavaMailSender sender;

    public SimpleMailSender(JavaMailSender sender) {
        this.sender = sender;
    }

    @Override
    public void send(String to, String subject, String text) throws NullPointerException {
        if(to == null || subject == null || text == null)
            throw new NullPointerException();
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(String.format(text));
        sender.send(message);
    }
}
