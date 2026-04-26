package ru.reel.SecurityService.infrastructure.mail;

import org.springframework.stereotype.Component;
import ru.reel.SecurityService.port.mail.MailOtpSender;

@Component
public class SimpleMailOtpSender implements MailOtpSender {
    private final SimpleMailSender sender;
    private static final String SUBJECT = "Reel - OTP-code";
    private static final String TEXT = "%s";


    public SimpleMailOtpSender(SimpleMailSender sender) {
        this.sender = sender;
    }

    @Override
    public void send(String to, String code) throws NullPointerException {
        if(to == null || code == null)
            throw new NullPointerException();
        sender.send(to, SUBJECT, String.format(TEXT, code));
    }
}
