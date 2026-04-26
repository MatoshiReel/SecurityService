package ru.reel.SecurityService.service.token;

import java.security.NoSuchAlgorithmException;

public interface TokenGenerator {
    String generate() throws NoSuchAlgorithmException;
}
