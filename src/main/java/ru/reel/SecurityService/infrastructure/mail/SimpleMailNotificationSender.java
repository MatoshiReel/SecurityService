package ru.reel.SecurityService.infrastructure.mail;

import org.springframework.stereotype.Component;
import ru.reel.SecurityService.port.mail.MailNotificationSender;

@Component
public class SimpleMailNotificationSender implements MailNotificationSender<ModifiedParameter> {
    private final SimpleMailSender sender;
    private static final String SUBJECT = "Reel - security notification";

    public SimpleMailNotificationSender(SimpleMailSender sender) {
        this.sender = sender;
    }

    @Override
    public void send(String to, ModifiedParameter modifiedParameter) throws NullPointerException, IllegalArgumentException {
        if(to == null || modifiedParameter == null)
            throw new NullPointerException();
        sender.send(to, SUBJECT, this.getText(modifiedParameter));
    }

    private String getText(ModifiedParameter modifiedParameter) throws IllegalArgumentException {
        return switch (modifiedParameter) {
            case EMAIL -> "Someone wants to change email. If it is you, you can ignore this message.";
        };
    }
}
