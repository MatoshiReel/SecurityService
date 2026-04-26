package ru.reel.SecurityService.infrastructure.redis;

import lombok.NonNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Component;
import ru.reel.SecurityService.port.redis.EmailTokenTemplate;

import java.time.Duration;
import java.util.Objects;

@Component
public class RedisEmailTokenTemplate implements EmailTokenTemplate {
    private final RedisOperations<String, String> operations;
    private final Argon2PasswordEncoder encoder;
    private static final String KEY_FORMAT = "token:change:email:%s";
    private static final String KEY_ATTEMPT_FORMAT = KEY_FORMAT + ":attempts";
    private static final Duration TOKEN_TTL = Duration.ofMinutes(5);

    public RedisEmailTokenTemplate(@Qualifier("redisOperations") RedisOperations<String, String> operations, Argon2PasswordEncoder encoder) {
        this.operations = operations;
        this.encoder = encoder;
    }

    @Override
    public void set(@NonNull String accountId, @NonNull String token) throws NullPointerException {
        operations.opsForValue().set(String.format(KEY_FORMAT, accountId), Objects.requireNonNull(encoder.encode(token)), TOKEN_TTL);
    }

    @Override
    public void setWithAttempts(@NonNull String accountId, @NonNull String token, short attempts) throws NullPointerException {
        this.set(accountId, token);
        operations.opsForValue().set(String.format(KEY_ATTEMPT_FORMAT, accountId), String.valueOf(attempts), TOKEN_TTL);
    }

    @Override
    public boolean verify(@NonNull String rawToken, @NonNull String accountId) throws NullPointerException {
        return encoder.matches(rawToken, this.getToken(accountId));
    }

    @Override
    public String getToken(@NonNull String accountId) throws NullPointerException {
        return operations.opsForValue().get(String.format(KEY_FORMAT, accountId));
    }

    @Override
    public short getAttempts(@NonNull String accountId) throws NullPointerException {
        String attempts = operations.opsForValue().get(String.format(KEY_ATTEMPT_FORMAT, accountId));
        if(attempts == null)
            return 0;
        return Short.parseShort(attempts);
    }

    @Override
    public Duration getTokenTtl() {
        return TOKEN_TTL;
    }


    @Override
    public void decrementAttempts(@NonNull String accountId) throws NullPointerException {
        operations.opsForValue().decrement(String.format(KEY_ATTEMPT_FORMAT, accountId));
    }

    @Override
    public void delToken(@NonNull String accountId) throws NullPointerException {
        operations.delete(String.format(KEY_FORMAT, accountId));
    }

    @Override
    public void delTokenWithAttempts(@NonNull String accountId) throws NullPointerException {
        this.delToken(accountId);
        operations.delete(String.format(KEY_ATTEMPT_FORMAT, accountId));
    }
}
