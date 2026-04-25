package ru.reel.SecurityService.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.reel.SecurityService.dto.NewLoginDto;
import ru.reel.SecurityService.dto.NewPasswordDto;
import ru.reel.SecurityService.service.AccountService;
import ru.reel.SecurityService.service.validator.LoginValidator;
import ru.reel.SecurityService.service.validator.PasswordValidator;
import ru.reel.SecurityService.service.validator.RequestValidator;
import ru.reel.request.error.RequestError;
import ru.reel.request.error.RequestFieldError;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(path = "/security")
public class SecurityController {
    private final RequestValidator requestValidator;
    private final PasswordValidator passwordValidator;
    private final LoginValidator loginValidator;
    private final AccountService service;

    public SecurityController(RequestValidator requestValidator, PasswordValidator passwordValidator, LoginValidator loginValidator, AccountService service) {
        this.requestValidator = requestValidator;
        this.passwordValidator = passwordValidator;
        this.loginValidator = loginValidator;
        this.service = service;
    }

    @PostMapping(path = "/password/change", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> changePassword(@RequestBody(required = false) NewPasswordDto newPasswordDto, @RequestHeader("X-Account-Id") String accountId) {
        ResponseEntity<RequestError> emptyBodyResponse = requestValidator.checkBodyEmpty(newPasswordDto);
        if(emptyBodyResponse != null)
            return emptyBodyResponse;
        passwordValidator.setAccountId(UUID.fromString(accountId));
        List<RequestFieldError> errors = passwordValidator.validate(newPasswordDto);
        if(!errors.isEmpty())
            return ResponseEntity.badRequest().body(errors);
        service.updatePasswordById(newPasswordDto.newPassword, accountId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(path = "/login/change", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> changeLogin(@RequestBody(required = false) NewLoginDto newLoginDto, @RequestHeader("X-Account-Id") String accountId) {
        ResponseEntity<RequestError> emptyBodyResponse = requestValidator.checkBodyEmpty(newLoginDto);
        if(emptyBodyResponse != null)
            return emptyBodyResponse;
        loginValidator.setAccountId(UUID.fromString(accountId));
        List<RequestFieldError> errors = loginValidator.validate(newLoginDto);
        if(!errors.isEmpty())
            return ResponseEntity.badRequest().body(errors);
        service.updateLoginById(newLoginDto.newLogin, accountId);
        return ResponseEntity.noContent().build();
    }
}
