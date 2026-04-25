package ru.reel.SecurityService.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@Getter
@Entity
@Table(name = "accounts")
@NoArgsConstructor
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Setter
    @Column(name = "login", nullable = false, unique = true, length = 20)
    private String login;

    @Setter
    @Column(name = "password", nullable = false, length = 97)
    private String password;

    @Column(name = "email", unique = true, length = 255)
    private String email;

    @Column(name = "pending_email", unique = true)
    private String pendingEmail;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified;

    @Column(name = "created_at", nullable = false)
    private final Date createdAt = new Date();

    @Column(name = "2fa_enabled")
    private final Boolean is2FaEnabled = false;
}
