package ru.reel.SecurityService.port.redis;

import java.time.Duration;

public interface EmailTokenTemplate {
    void set(String accountId, String token);
    void setWithAttempts(String accountId, String token, short attempts);
    boolean verify(String rawToken, String accountId);
    String getToken(String accountId);
    short getAttempts(String accountId);
    Duration getTokenTtl();
    void decrementAttempts(String accountId);
    void delToken(String accountId);
    void delTokenWithAttempts(String accountId);
}
