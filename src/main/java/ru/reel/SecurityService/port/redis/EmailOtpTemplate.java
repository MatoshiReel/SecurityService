package ru.reel.SecurityService.port.redis;

import java.time.Duration;

public interface EmailOtpTemplate {
    void set(String email, String code);
    void setWithAttempts(String email, String code, short attempts);
    boolean verify(String rawCode, String email);
    String getCode(String email);
    short getAttempts(String email);
    Duration getCodeTtl();
    void decrementAttempts(String email);
    void delCode(String email);
    void delCodeWithAttempts(String email);
}
