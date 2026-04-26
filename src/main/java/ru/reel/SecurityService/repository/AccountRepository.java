package ru.reel.SecurityService.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.reel.SecurityService.entity.Account;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends CrudRepository<Account, UUID> {
    @Modifying
    @Transactional
    @Query("UPDATE Account a SET a.password = :password WHERE a.id = :id")
    void updatePasswordById(String password, UUID id);
    @Modifying
    @Transactional
    @Query("UPDATE Account a SET a.login = :login WHERE a.id = :id")
    void updateLoginById(String login, UUID id);
    @Modifying
    @Transactional
    @Query("UPDATE Account a SET a.email = :email WHERE a.id = :id")
    void updateEmailById(String email, UUID id);
    @Modifying
    @Transactional
    @Query("UPDATE Account a SET a.pendingEmail = :pendingEmail WHERE a.id = :id")
    void updatePendingEmailById(String pendingEmail, UUID id);
    @Modifying
    @Transactional
    @Query("UPDATE Account a SET a.emailVerified = :emailVerified WHERE a.id = :id")
    void updateEmailVerifiedById(boolean emailVerified, UUID id);
    @Modifying
    @Transactional
    @Query("UPDATE Account a SET a.is2FaEnabled = :is2FaEnabled WHERE a.id = :id")
    void update2FaEnabledById(boolean is2FaEnabled, UUID id);
    Optional<Account> findByLogin(String login);
    Optional<Account> findByEmail(String email);
}
