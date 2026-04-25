package ru.reel.SecurityService.service;

import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.reel.SecurityService.repository.AccountRepository;

import java.util.UUID;

@Service
public class AccountService {
    private final AccountRepository repository;
    private final Argon2PasswordEncoder encoder;

    public AccountService(AccountRepository repository, Argon2PasswordEncoder encoder) {
        this.repository = repository;
        this.encoder = encoder;
    }

    public void updatePasswordById(String newPassword, String id) throws NullPointerException, IllegalArgumentException {
        if(newPassword == null || id == null)
            throw new NullPointerException();
        repository.updatePasswordById(encoder.encode(newPassword), UUID.fromString(id));
    }

    public void updateLoginById(String newLogin, String id) throws NullPointerException, IllegalArgumentException {
        if(newLogin == null || id == null)
            throw new NullPointerException();
        repository.updateLoginById(newLogin, UUID.fromString(id));
    }
}
