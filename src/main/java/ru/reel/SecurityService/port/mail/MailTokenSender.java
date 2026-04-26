package ru.reel.SecurityService.port.mail;

public interface MailTokenSender {
    void send(String to, String token);
}
