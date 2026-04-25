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
    Optional<Account> findByLogin(String login);
}
