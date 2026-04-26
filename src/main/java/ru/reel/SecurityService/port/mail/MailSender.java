package ru.reel.SecurityService.port.mail;

public interface MailSender {
    void send(String to, String subject, String text);
}
