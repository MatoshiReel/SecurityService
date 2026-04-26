package ru.reel.SecurityService.port.mail;

public interface MailOtpSender {
    void send(String to, String code);
}
