package ru.reel.SecurityService.service;

import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.reel.SecurityService.entity.Account;
import ru.reel.SecurityService.repository.AccountRepository;

import java.util.Optional;
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

    public void updateEmailById(String newEmail, String id) throws NullPointerException, IllegalArgumentException {
        if(newEmail == null || id == null)
            throw new NullPointerException();
        repository.updateEmailById(newEmail, UUID.fromString(id));
    }

    public void updatePendingEmailById(String newEmail, String id) throws NullPointerException, IllegalArgumentException {
        if(id == null)
            throw new NullPointerException();
        repository.updatePendingEmailById(newEmail, UUID.fromString(id));
    }

    public void updateEmailVerifiedById(boolean isVerified, String id) throws NullPointerException, IllegalArgumentException {
        if(id == null)
            throw new NullPointerException();
        repository.updateEmailVerifiedById(isVerified, UUID.fromString(id));
    }

    public String getEmailById(String accountId) throws NullPointerException, IllegalArgumentException {
        if(accountId == null)
            throw new NullPointerException();
        Optional<Account> account = repository.findById(UUID.fromString(accountId));
        return account.map(Account::getEmail).orElse(null);
    }

    public String getPendingEmailById(String accountId) throws NullPointerException, IllegalArgumentException {
        if(accountId == null)
            throw new NullPointerException();
        Optional<Account> account = repository.findById(UUID.fromString(accountId));
        return account.map(Account::getPendingEmail).orElse(null);
    }

    public boolean isExistsByEmail(String email) throws NullPointerException {
        if(email == null)
            throw new NullPointerException();
        return repository.findByEmail(email).isPresent();
    }
}
