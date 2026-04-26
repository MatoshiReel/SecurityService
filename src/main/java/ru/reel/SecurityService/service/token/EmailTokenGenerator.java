package ru.reel.SecurityService.service.token;

import org.springframework.stereotype.Component;

import javax.crypto.KeyGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Component
public class EmailTokenGenerator implements TokenGenerator {
    @Override
    public String generate() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(128);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(keyGenerator.generateKey().getEncoded());
    }
}
