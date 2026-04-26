package ru.reel.SecurityService.infrastructure.mail;

import org.springframework.stereotype.Component;
import ru.reel.SecurityService.port.mail.MailTokenSender;

@Component
public class SimpleMailTokenSender implements MailTokenSender {
    private final SimpleMailSender sender;
    private static final String SUBJECT = "Reel - token";
    private static final String TEXT = "%s";


    public SimpleMailTokenSender(SimpleMailSender sender) {
        this.sender = sender;
    }

    @Override
    public void send(String to, String token) throws NullPointerException {
        if(to == null || token == null)
            throw new NullPointerException();
        sender.send(to, SUBJECT, String.format(TEXT, token));
    }
}
