package ru.reel.SecurityService.port.mail;

public interface MailNotificationSender<P> {
    void send(String to, P modifiedParameter);
}
