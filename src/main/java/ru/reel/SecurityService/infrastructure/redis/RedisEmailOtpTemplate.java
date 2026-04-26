package ru.reel.SecurityService.infrastructure.redis;

import lombok.NonNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Component;
import ru.reel.SecurityService.port.redis.EmailOtpTemplate;

import java.time.Duration;
import java.util.Objects;

@Component
public class RedisEmailOtpTemplate implements EmailOtpTemplate {
    private final RedisOperations<String, String> operations;
    private final Argon2PasswordEncoder encoder;
    private static final String KEY_FORMAT = "otp:2fa:switch:email:%s";
    private static final String KEY_ATTEMPT_FORMAT = KEY_FORMAT + ":attempts";
    private static final Duration TOKEN_TTL = Duration.ofMinutes(10);

    public RedisEmailOtpTemplate(@Qualifier("redisOperations") RedisOperations<String, String> operations, Argon2PasswordEncoder encoder) {
        this.operations = operations;
        this.encoder = encoder;
    }

    @Override
    public void set(@NonNull String email, @NonNull String code) throws NullPointerException {
        operations.opsForValue().set(String.format(KEY_FORMAT, email), Objects.requireNonNull(encoder.encode(code)), TOKEN_TTL);
    }

    @Override
    public void setWithAttempts(@NonNull String email, @NonNull String code, short attempts) throws NullPointerException {
        this.set(email, code);
        operations.opsForValue().set(String.format(KEY_ATTEMPT_FORMAT, email), String.valueOf(attempts), TOKEN_TTL);
    }

    @Override
    public boolean verify(@NonNull String rawCode, @NonNull String email) throws NullPointerException {
        return encoder.matches(rawCode, this.getCode(email));
    }

    @Override
    public String getCode(@NonNull String email) throws NullPointerException {
        return operations.opsForValue().get(String.format(KEY_FORMAT, email));
    }

    @Override
    public short getAttempts(@NonNull String email) throws NullPointerException {
        String attempts = operations.opsForValue().get(String.format(KEY_ATTEMPT_FORMAT, email));
        if(attempts == null)
            return 0;
        return Short.parseShort(attempts);
    }

    @Override
    public Duration getCodeTtl() {
        return TOKEN_TTL;
    }


    @Override
    public void decrementAttempts(@NonNull String email) throws NullPointerException {
        operations.opsForValue().decrement(String.format(KEY_ATTEMPT_FORMAT, email));
    }

    @Override
    public void delCode(@NonNull String email) throws NullPointerException {
        operations.delete(String.format(KEY_FORMAT, email));
    }

    @Override
    public void delCodeWithAttempts(@NonNull String email) throws NullPointerException {
        this.delCode(email);
        operations.delete(String.format(KEY_ATTEMPT_FORMAT, email));
    }
}
